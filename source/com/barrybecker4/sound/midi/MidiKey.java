package com.barrybecker4.sound.midi;

import java.awt.Rectangle;

/**
 * Black or white piano key that triggers MIDI note on/off.
 */
class MidiKey extends Rectangle {

    /** {@link MidiSynth#ON} or {@link MidiSynth#OFF}. */
    int noteState = MidiSynth.OFF;
    /** MIDI note number for this key. */
    final int kNum;
    private final MidiSynth synth;

    /**
     * @param synth owning synthesizer panel
     * @param x left
     * @param y top
     * @param width pixel width
     * @param height pixel height
     * @param num MIDI note number
     */
    MidiKey(MidiSynth synth, int x, int y, int width, int height, int num) {
        super(x, y, width, height);
        this.synth = synth;
        this.kNum = num;
    }

    /** @return whether the key is currently held */
    boolean isNoteOn() {
        return noteState == MidiSynth.ON;
    }

    /** Sends note-on and optionally records it. */
    void on() {
        setNoteState(MidiSynth.ON);
        ChannelData cc = synth.cc;
        cc.channel.noteOn(kNum, cc.velocity);
        if (synth.record) {
            synth.createShortEvent(MidiSynth.NOTEON, kNum);
        }
    }

    /** Sends note-off and optionally records it. */
    void off() {
        setNoteState(MidiSynth.OFF);
        ChannelData cc = synth.cc;
        cc.channel.noteOff(kNum, cc.velocity);
        if (synth.record) {
            synth.createShortEvent(MidiSynth.NOTEOFF, kNum);
        }
    }

    /** @param state {@link MidiSynth#ON} or {@link MidiSynth#OFF} */
    void setNoteState(int state) {
        noteState = state;
    }
}
