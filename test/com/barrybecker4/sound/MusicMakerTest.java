// Copyright by Barry G. Becker, 2012. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound;

import com.barrybecker4.common.concurrency.ThreadUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Barry Becker
 */
public class MusicMakerTest  {

    private static final int DURATION = 2;

    /** instance under test. */
    private static final MusicMaker music = new MusicMaker();

    @Test
    public void testNumInstruments() {

        Instruments instruments = music.getInstruments();
        int numInstruments = instruments.getNumInstruments();
        assertEquals("Actual num instruments was " + numInstruments, 235, numInstruments );
    }

    @Test
    public void testPlayAllInstruments() {

        Instruments instruments = music.getInstruments();
        for (int i = 0; i < instruments.getNumInstruments(); i++) {
            music.playNote(i / 8, i % 8, 50, 1, DURATION, 200);
        }
    }

    @Test
    public void testPlayInstrumentsSimultaneously() {

        Instruments instruments = music.getInstruments();

        for (int i = 0; i < instruments.getNumInstruments(); i++) {
            music.startNote(i / 8, i % 8, 80, i % 16, 200);
            ThreadUtil.sleep(2 * DURATION);
        }
        music.stopAllSounds();
    }

}

