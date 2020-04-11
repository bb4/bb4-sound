package com.barrybecker4.sound.midi;

import javax.sound.midi.MidiChannel;

/**
 * Stores MidiChannel information.
 */
class ChannelData {

    MidiChannel channel;
    boolean solo, mono, mute, sustain;
    int velocity, pressure, bend, reverb;
    int row, col, num;

    ChannelData(MidiChannel channel, int num) {
        this.channel = channel;
        this.num = num;
        this.velocity = this.pressure = this.bend = this.reverb = 64;
    }
}