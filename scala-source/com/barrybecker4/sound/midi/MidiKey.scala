package com.barrybecker4.sound.midi

import java.awt.Rectangle

/** Black or white piano key that triggers MIDI note on/off. */
class MidiKey(synth: MidiSynth, x: Int, y: Int, width: Int, height: Int, val kNum: Int)
    extends Rectangle(x, y, width, height) {

  var noteState: Int = MidiSynth.OFF

  def isNoteOn: Boolean = noteState == MidiSynth.ON

  def on(): Unit = {
    setNoteState(MidiSynth.ON)
    val cc = synth.cc
    cc.channel.noteOn(kNum, cc.velocity)
    if (synth.record) synth.createShortEvent(MidiSynth.NOTEON, kNum)
  }

  def off(): Unit = {
    setNoteState(MidiSynth.OFF)
    val cc = synth.cc
    cc.channel.noteOff(kNum, cc.velocity)
    if (synth.record) synth.createShortEvent(MidiSynth.NOTEOFF, kNum)
  }

  def setNoteState(state: Int): Unit = noteState = state
}
