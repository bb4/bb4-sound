package com.barrybecker4.sound.midi

import javax.sound.midi.MidiChannel

/** Stores MidiChannel information and per-channel controller UI state. */
private[midi] class ChannelData(val channel: MidiChannel, val num: Int) {
  var solo = false
  var mono = false
  var mute = false
  var sustain = false
  var velocity = 64
  var pressure = 64
  var bend = 64
  var reverb = 64
  var row = 0
  var col = 0
}
