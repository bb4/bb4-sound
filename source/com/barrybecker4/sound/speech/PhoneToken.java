/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech;

import com.barrybecker4.common.concurrency.ThreadUtil;
import com.barrybecker4.common.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;

/**
 * Represents a single phonetic token.
 * See http://www.javaworld.com/javaworld/jw-08-2001/jw-0817-javatalk.html?page=1

 * Allophones, are labeled with a one-, two-, or three-letter identifier.
 * Some identifiers are obvious and some not so obvious, as you can see from the phonetic
 * representation of the word "hello."

    h -- sounds as you would expect
    e -- sounds as you would expect
    l -- sounds as you would expect, but notice that I've reduced a double "l" to a single one
    oo -- is the sound for "hello," not for "bot," and not for "too"
 */
class PhoneToken {

    private static final String RESOURCE_PATH = "com/barrybecker4/sound/allophones/";
    private static final int COMMA_PAUSE = 200;
    private static final int PERIOD_PAUSE = 680;
    private static final int MAX_MERGE_COUNT = 1000;

    /** delay in millis between words. */
    private static final int DELAY_BETWEEN_WORDS = 200;

    private String phoneToken;
    private SourceDataLine line = null;

    /**
     * Constructor.
     * This method speaks the given phonetic word.
     * Construct a file name for the allophone and load/say it
     */
    PhoneToken(String token) {
        phoneToken = token;
    }


    /** Speak the next allophone unless it is punctuation, in which case we just pause. */
    byte[] process(byte[] previousSound) {

        if (!pauseForPunctuation()) {

            String phoneFile = RESOURCE_PATH + phoneToken + ".au";

            // -- get the data from the file --
            byte[] thisSound = getSound( phoneFile );
            assert (thisSound.length > 0) : "Invalid sound file: " + phoneFile;

            return sayPhoneToken(previousSound, thisSound);
        }
        return previousSound;
    }

    /** Pause appropriately if we have encountered punctuation. */
    private boolean pauseForPunctuation() {
        if (phoneToken.equals(",") || phoneToken.equals(".")) {
            if (phoneToken.equals(",")) {
                ThreadUtil.sleep(COMMA_PAUSE);
            }
            else {
                ThreadUtil.sleep(PERIOD_PAUSE);
            }
            return true;
        }
        return false;
    }

    /*
     * This method drains the sound channel.
     */
    void drain() {
        if ( line != null )  {
            // this used to be just drain, but I added flush to make it work post java 1.5
            line.drain();
            line.flush();
            ThreadUtil.sleep(100);

        }
        ThreadUtil.sleep(DELAY_BETWEEN_WORDS);
    }

    /**
     * Speak a single phonetic token
     * @return the token that was said.
     */
    private byte[] sayPhoneToken(byte[] previousSound, byte[] thisSound) {
        if ( previousSound != null ) {
            // -- merge the previous allophone with this one if we can --
            int mergeCount = 0;
            if ( previousSound.length >= MAX_MERGE_COUNT && thisSound.length >= MAX_MERGE_COUNT )  {
                mergeCount = MAX_MERGE_COUNT;
            }
            for ( int i = 0; i < mergeCount; i++ ) {
                previousSound[previousSound.length - mergeCount + i]
                        = (byte) ((previousSound[previousSound.length - mergeCount + i] + thisSound[i]) / 2);
            }

            // -- play the previous allophone --
            playSound( previousSound );

            // -- set the truncated current allophone as previous --
            byte[] newSound = new byte[thisSound.length - mergeCount];
            System.arraycopy(thisSound, mergeCount, newSound, 0, newSound.length);
            return newSound;
        }
        else {
            return thisSound;
        }
    }

    /**
     * This method plays a sound sample.
     */
    public void playSound( byte[] data ) {
        if (data == null) return;
        if (line == null) {
            throw new IllegalArgumentException("Problem processing " + phoneToken);
        }
        if ( data.length > 0 ) {
            line.write( data, 0, data.length );
            ThreadUtil.sleep(100);
        }
    }

    /**
     * This method reads the file for a single allophone and constructs a byte vector.
     */
    private byte[] getSound( String sPath ) {
        try {
            //URL url = SpeechSynthesizer.class.getResource( fileName );
            URL url = FileUtil.getURL(sPath);
            AudioInputStream stream = AudioSystem.getAudioInputStream( url );
            AudioFormat format = stream.getFormat();

            // -- convert an ALAW/ULAW sound to PCM for playback --
            if ( (format.getEncoding() == AudioFormat.Encoding.ULAW) ||
                    (format.getEncoding() == AudioFormat.Encoding.ALAW) ) {
                AudioFormat tmpFormat = createAudioFormat(format);
                stream = AudioSystem.getAudioInputStream( tmpFormat, stream );
                format = tmpFormat;
            }

            if ( line == null ) {
                line = createDataLine(format);
            }

            // -- some size calculations --
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() >> 3;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

            byte[] data = new byte[bufferLengthInBytes];

            // -- read the data bytes and count them --
            int numBytesRead = 0;
            numBytesRead = stream.read(data);

            byte maxByte = 0;

            // -- truncate the byte array to the correct size --
            byte[] newData = new byte[numBytesRead];
            for ( int i = 0; i < numBytesRead; i++ ) {
                newData[i] = data[i];
                if ( newData[i] > maxByte ) maxByte = newData[i];
            }

            return newData;
        } catch (Exception e) {
            System.out.println( "Something wrong with " + sPath );
            e.printStackTrace();
            return new byte[0];
        }
    }

    private AudioFormat createAudioFormat(AudioFormat format) {
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            format.getSampleSizeInBits() << 1,
            format.getChannels(),
            format.getFrameSize() << 1,
            format.getFrameRate(),
            true );
    }

    private SourceDataLine createDataLine(AudioFormat format) throws Exception {
        SourceDataLine dataLine;
        // -- output line not instantiated yet --
        // -- can we find a suitable kind of line? --
        DataLine.Info outInfo = new DataLine.Info( SourceDataLine.class, format );
        if ( !AudioSystem.isLineSupported( outInfo ) ) {
            throw new Exception( "Line matching " + outInfo + " not supported." );
        }

        // -- open the source data line (the output line) --
        dataLine = (SourceDataLine) AudioSystem.getLine( outInfo );
        dataLine.open( format, 50000 );
        dataLine.start();
        return dataLine;
    }
}
