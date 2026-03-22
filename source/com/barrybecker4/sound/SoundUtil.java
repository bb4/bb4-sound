package com.barrybecker4.sound;

import com.barrybecker4.common.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Static methods for playing sound files.
 * @author Barry Becker
 */
public final class SoundUtil {

    private static final ExecutorService PLAYBACK = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "bb4-sound-playback");
        t.setDaemon(true);
        return t;
    });

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
     * This converts the file to something that java can play.
     * wav, aiff, and au files should work, but currently only au seem to.
     * @param soundPath path to sound file
     */
    public static void playSound(final String soundPath) {
        playAsync(() -> playSoundInternal(FileUtil.getURL(soundPath)));
    }

     /**
     * This converts the file to something that java can play.
     * wav, aiff, and au files should work, but currently only au seem to.
     * @param clipURL url of audio clip
     */
    public static void playSound(final URL clipURL) {
        playAsync(() -> playSoundInternal(clipURL));
    }

    private static void playAsync(Runnable task) {
        PLAYBACK.execute(task);
    }

    /**
     * This converts the file to something that java can play.
     * @param clipURL url of audio clip
     */
    private static void playSoundInternal(URL clipURL) {
        try (AudioInputStream fis = AudioSystem.getAudioInputStream(clipURL);
             AudioInputStream ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, fis)) {
            AudioFormat af = ais.getFormat();
            try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                line.open(af);
                int bufSize = line.getBufferSize();

                line.start();

                byte[] data = new byte[bufSize];
                int bytesRead;

                while ((bytesRead = ais.read(data, 0, data.length)) != -1) {
                    line.write(data, 0, bytesRead);
                }

                line.drain();
                line.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SoundUtil() {}
}
