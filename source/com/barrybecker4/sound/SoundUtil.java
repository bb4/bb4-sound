package com.barrybecker4.sound;

import com.barrybecker4.common.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;


/**
 * Static methods for playing sound files.
 * @author Barry Becker
 */
public final class SoundUtil {

    /**
     * This converts the file to something that java can play.
     * wav, aiff, and au files should work, but currently only au seem to.
     * @param soundPath path to sound file
     */
    public static void playSoundAndWait(String soundPath) {
        URL clipURL = FileUtil.getURL(soundPath);
        playSoundInternal(clipURL);
    }

    /**
     * This converts the file to something that jav can paly.
     * wav, aiff, and au files should work, but currently only au seem to.
     * @param soundPath path to sound file
     */
    public static void playSound(final String soundPath) {

        Runnable playSoundTask = new Runnable() {
            public void run() {
                URL clipURL = FileUtil.getURL(soundPath);
                playSoundInternal(clipURL);
            }
        };

        Thread t = new Thread(playSoundTask);
        t.start();
    }

     /**
     * This converts the file to something that jav can play.
     * wav, aiff, and au files should work, but currently only au seem to.
     * @param clipURL url of audio clip
     */
    public static void playSound(final URL clipURL) {

         Runnable playSoundTask = new Runnable() {
            public void run() {
                playSoundInternal(clipURL);
            }
        };

        Thread t = new Thread(playSoundTask);
        t.start();
    }

    /**
     * This converts the file to something that java can play.
     * @param clipURL url of audio clip
     */
    private static void playSoundInternal(URL clipURL) {
        try {
            AudioInputStream fis =  AudioSystem.getAudioInputStream(clipURL);
            //System.out.println("File AudioFormat: " + fis.getFormat());

            AudioInputStream ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, fis);
            AudioFormat af = ais.getFormat();
            //System.out.println("AudioFormat: " + af.toString());

            SourceDataLine line = AudioSystem.getSourceDataLine(af);

            line.open(af);
            int bufSize = line.getBufferSize();

            line.start();

            byte[] data = new byte[bufSize];
            int bytesRead;

            while ((bytesRead = ais.read(data,0,data.length)) != -1) {
                line.write(data,0,bytesRead);
            }

            line.drain();
            line.stop();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SoundUtil() {}
}
