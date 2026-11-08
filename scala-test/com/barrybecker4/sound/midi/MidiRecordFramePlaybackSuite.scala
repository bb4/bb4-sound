package com.barrybecker4.sound.midi

import org.scalatest.funsuite.AnyFunSuite

import javax.sound.midi.{MidiEvent, MidiSystem, Sequence, ShortMessage}

/** Headless coverage for capture Play rewind — no display, synthesizer, or mixer. */
class MidiRecordFramePlaybackSuite extends AnyFunSuite {

  test("preparePlayback rewinds when the same Sequence is set again") {
    val sequencer = MidiSystem.getSequencer(false)
    try {
      sequencer.open()
      val sequence = oneNoteSequence(tick = 100)
      sequencer.setSequence(sequence)
      sequencer.setTickPosition(sequencer.getTickLength)
      assert(sequencer.getTickPosition > 0, "precondition: tick must be at end before prepare")

      MidiRecordFrame.preparePlayback(sequencer, sequence)

      assertResult(0L)(sequencer.getTickPosition)
    } finally {
      if (sequencer.isOpen) sequencer.close()
    }
  }

  test("preparePlayback rewinds from a mid-track position") {
    val sequencer = MidiSystem.getSequencer(false)
    try {
      sequencer.open()
      val sequence = oneNoteSequence(tick = 200)
      sequencer.setSequence(sequence)
      sequencer.setTickPosition(75)
      assertResult(75L)(sequencer.getTickPosition)

      MidiRecordFrame.preparePlayback(sequencer, sequence)

      assertResult(0L)(sequencer.getTickPosition)
    } finally {
      if (sequencer.isOpen) sequencer.close()
    }
  }

  private def oneNoteSequence(tick: Long): Sequence = {
    val sequence = new Sequence(Sequence.PPQ, 10)
    val track = sequence.createTrack()
    val noteOn = new ShortMessage()
    noteOn.setMessage(ShortMessage.NOTE_ON, 0, 60, 64)
    track.add(new MidiEvent(noteOn, tick))
    sequence
  }
}
