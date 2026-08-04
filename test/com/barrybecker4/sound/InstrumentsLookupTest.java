package com.barrybecker4.sound;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class InstrumentsLookupTest {

    @Test
    void getInstrumentIndex_findsExistingInstrumentByName() throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        try {
            Soundbank sb = synth.getDefaultSoundbank();
            Assumptions.assumeTrue(sb != null, "No default soundbank");
            Instruments instruments = new Instruments(sb);
            String name = instruments.getInstrument(0).getName();
            int idx = instruments.getInstrumentIndex(name);
            assertTrue(idx >= 0, "Lookup by exact name from bank should succeed");
        } finally {
            synth.close();
        }
    }

    @Test
    void getInstrumentIndex_returnsNegativeForMissingName() throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        try {
            Soundbank sb = synth.getDefaultSoundbank();
            Assumptions.assumeTrue(sb != null, "No default soundbank");
            Instruments instruments = new Instruments(sb);
            assertTrue(instruments.getInstrumentIndex("___no_such_instrument___") < 0);
        } finally {
            synth.close();
        }
    }

    @Test
    void getInstrumentName_matchesFlatIndexLayout() throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        try {
            Soundbank sb = synth.getDefaultSoundbank();
            Assumptions.assumeTrue(sb != null, "No default soundbank");
            Instruments instruments = new Instruments(sb);
            Assumptions.assumeTrue(instruments.getNumInstruments() > 8, "Need at least one full row");
            // Layout is column-major groups of 8: index = subType + 8 * type
            String expected = instruments.getInstrument(9).getName();
            assertEquals(expected, instruments.getInstrumentName(1, 1));
        } finally {
            synth.close();
        }
    }
}
