// Copyright by Barry G. Becker, 2013. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound.midi;

import javax.sound.midi.Track;

/**
 * A recorded MIDI track with display metadata for the capture table.
 */
class TrackData {

    Integer chanNum;
    String name;
    final Track track;

    TrackData(int chanNum, String name, Track track) {
        this.chanNum = chanNum;
        this.name = name;
        this.track = track;
    }
}
