// Copyright by Barry G. Becker, 2012. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound;

import com.barrybecker4.common.concurrency.ThreadUtil;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 *
 * @author Barry Becker
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class MusicMakerTest  {

    private static final int DURATION = 2;

    /** instance under test. */
    private static final MusicMaker music = new MusicMaker();
    private static final Instruments instruments = music.getInstruments();

    @Test
    public void testNumInstruments() {
        Assumptions.assumeTrue(instruments != null, "No MIDI soundbank (e.g. headless CI)");
        int numInstruments = instruments.getNumInstruments();
        assertTrue(numInstruments > 0, "Actual num instruments was " + numInstruments);
    }

    @Test
    public void testPlayInstrumentsSimultaneously() {
        Assumptions.assumeTrue(instruments != null, "No MIDI soundbank (e.g. headless CI)");
        int numInstruments = instruments.getNumInstruments();
        for (int i = 0; i < numInstruments; i++) {
            music.startNote(i / 8, i % 8, 80, i % 16, 200);
            ThreadUtil.sleep(2 * DURATION);
        }
        music.stopAllSounds();
    }

    @Test
    public void testPlayAllInstruments() {
        Assumptions.assumeTrue(instruments != null, "No MIDI soundbank (e.g. headless CI)");
        int numInstruments = instruments.getNumInstruments();
        for (int i = 0; i < numInstruments; i++) {
            music.playNote(i / 8, i % 8, 50, 1, DURATION, 200);
        }
    }
}
