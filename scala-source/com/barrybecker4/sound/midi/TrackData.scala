// Copyright by Barry G. Becker, 2013. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound.midi

import javax.sound.midi.Track

/** A recorded MIDI track with display metadata for the capture table. */
class TrackData(initialChanNum: Int, initialName: String, val track: Track) {
  var chanNum: Integer = Integer.valueOf(initialChanNum)
  var name: String = initialName
}
