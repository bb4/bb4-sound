package com.barrybecker4.sound.midi;

import javax.sound.midi.MidiChannel;

/**
 * Stores MidiChannel information and per-channel controller UI state.
 */
class ChannelData {

    final MidiChannel channel;
    final int num;

    boolean solo;
    boolean mono;
    boolean mute;
    boolean sustain;
    int velocity;
    int pressure;
    int bend;
    int reverb;
    int row;
    int col;

    ChannelData(MidiChannel channel, int num) {
        this.channel = channel;
        this.num = num;
        this.velocity = 64;
        this.pressure = 64;
        this.bend = 64;
        this.reverb = 64;
    }
}
