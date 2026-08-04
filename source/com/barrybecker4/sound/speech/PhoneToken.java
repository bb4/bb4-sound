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
import java.io.IOException;
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
    static final int MAX_MERGE_COUNT = 1000;

    /** delay in millis between words. */
    private static final int DELAY_BETWEEN_WORDS = 200;

    private final String phoneToken;
    private SourceDataLine line;

    /**
     * Constructor.
     * This method speaks the given phonetic word.
     * Construct a file name for the allophone and load/say it
     */
    PhoneToken(String token) {
        phoneToken = token;
    }

    /**
     * How many samples to blend at the boundary when both buffers are long enough.
     */
    static int computeMergeCount(byte[] previousSound, byte[] thisSound, int maxMergeThreshold) {
        if (previousSound.length >= maxMergeThreshold && thisSound.length >= maxMergeThreshold) {
            return maxMergeThreshold;
        }
        return 0;
    }

    static void mergeAllophonesInPlace(byte[] previousSound, byte[] thisSound, int mergeCount) {
        for (int i = 0; i < mergeCount; i++) {
            previousSound[previousSound.length - mergeCount + i]
                    = (byte) ((previousSound[previousSound.length - mergeCount + i] + thisSound[i]) / 2);
        }
    }

    static byte[] truncateAfterMerge(byte[] thisSound, int mergeCount) {
        byte[] newSound = new byte[thisSound.length - mergeCount];
        System.arraycopy(thisSound, mergeCount, newSound, 0, newSound.length);
        return newSound;
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
        if (isPunctuation()) {
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

    private boolean isPunctuation() {
        return (phoneToken.equals(",") || phoneToken.equals("."));
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
            int mergeCount = computeMergeCount(previousSound, thisSound, MAX_MERGE_COUNT);
            mergeAllophonesInPlace(previousSound, thisSound, mergeCount);

            // -- play the previous allophone --
            playSound( previousSound );

            // -- set the truncated current allophone as previous --
            return truncateAfterMerge(thisSound, mergeCount);
        }
        else {
            return thisSound;
        }
    }

    /**
     * This method plays a sound sample.
     */
    public void playSound( byte[] data ) {
        if (data == null || isPunctuation()) return;
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
            URL url = FileUtil.getURL(sPath);
            try (AudioInputStream stream = openPcmStream(url)) {
                AudioFormat format = stream.getFormat();
                if ( line == null ) {
                    line = createDataLine(format);
                }
                return readFirstSampleBuffer(stream, format);
            }
        } catch (Exception e) {
            System.err.println("Failed to load allophone audio: " + sPath);
            e.printStackTrace();
            return new byte[0];
        }
    }

    private AudioInputStream openPcmStream(URL url) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(url);
        AudioFormat format = stream.getFormat();
        if (format.getEncoding() == AudioFormat.Encoding.ULAW
                || format.getEncoding() == AudioFormat.Encoding.ALAW) {
            AudioFormat tmpFormat = createAudioFormat(format);
            return AudioSystem.getAudioInputStream(tmpFormat, stream);
        }
        return stream;
    }

    private byte[] readFirstSampleBuffer(AudioInputStream stream, AudioFormat format) throws IOException {
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() >> 3;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

        byte[] data = new byte[bufferLengthInBytes];

        int numBytesRead = stream.read(data);
        if (numBytesRead <= 0) {
            return new byte[0];
        }

        byte[] newData = new byte[numBytesRead];
        System.arraycopy(data, 0, newData, 0, numBytesRead);
        return newData;
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
        DataLine.Info outInfo = new DataLine.Info( SourceDataLine.class, format );
        if ( !AudioSystem.isLineSupported( outInfo ) ) {
            throw new Exception( "Line matching " + outInfo + " not supported." );
        }

        dataLine = (SourceDataLine) AudioSystem.getLine( outInfo );
        dataLine.open( format, 50000 );
        dataLine.start();
        return dataLine;
    }
}
