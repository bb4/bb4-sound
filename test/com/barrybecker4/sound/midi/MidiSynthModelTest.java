package com.barrybecker4.sound.midi;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiSynthModelTest {

    @Test
    void open_exposesChannelsWhenSoundbankPresent() {
        MidiSynthModel model = new MidiSynthModel();
        model.open();
        Assumptions.assumeTrue(model.hasInstruments(), "No default MIDI soundbank");
        assertNotNull(model.getMidiChannels());
        assertTrue(model.getMidiChannels().length > 0);
    }
}
