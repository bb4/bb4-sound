package com.barrybecker4.sound.midi

import org.scalatest.funsuite.AnyFunSuite

import javax.sound.midi.MidiChannel
import scala.collection.mutable.ListBuffer

/** Headless coverage for the Sustain checkbox — no display, synthesizer, or mixer. */
class MidiControlsSustainSuite extends AnyFunSuite {

  test("MidiSynth wires a Sustain checkbox") {
    val synth = new MidiSynth()
    assert(synth.sustCB != null)
    assertResult("Sustain")(synth.sustCB.getText)
  }

  test("Sustain checkbox sends controller 64 on then off") {
    val changes = ListBuffer.empty[(Int, Int)]
    val synth = new MidiSynth()
    synth.cc = new ChannelData(recordingChannel(changes), 0)

    synth.sustCB.setSelected(true)
    assert(synth.cc.sustain)
    assertResult(List((MidiSynth.SUSTAIN, 127)))(changes.toList)

    synth.sustCB.setSelected(false)
    assert(!synth.cc.sustain)
    assertResult(List((MidiSynth.SUSTAIN, 127), (MidiSynth.SUSTAIN, 0)))(changes.toList)
  }

  private def recordingChannel(changes: ListBuffer[(Int, Int)]): MidiChannel =
    new MidiChannel {
      override def noteOn(noteNumber: Int, velocity: Int): Unit = ()
      override def allNotesOff(): Unit = ()
      override def noteOff(noteNumber: Int, velocity: Int): Unit = ()
      override def noteOff(noteNumber: Int): Unit = ()
      override def setPolyPressure(noteNumber: Int, pressure: Int): Unit = ()
      override def getPolyPressure(noteNumber: Int): Int = 0
      override def setChannelPressure(pressure: Int): Unit = ()
      override def getChannelPressure: Int = 0
      override def controlChange(controller: Int, value: Int): Unit =
        changes += ((controller, value))
      override def getController(controller: Int): Int = 0
      override def programChange(program: Int): Unit = ()
      override def programChange(bank: Int, program: Int): Unit = ()
      override def getProgram: Int = 0
      override def setPitchBend(bend: Int): Unit = ()
      override def getPitchBend: Int = 0
      override def resetAllControllers(): Unit = ()
      override def allSoundOff(): Unit = ()
      override def localControl(on: Boolean): Boolean = false
      override def setMono(on: Boolean): Unit = ()
      override def getMono: Boolean = false
      override def setOmni(on: Boolean): Unit = ()
      override def getOmni: Boolean = false
      override def setMute(mute: Boolean): Unit = ()
      override def getMute: Boolean = false
      override def setSolo(solo: Boolean): Unit = ()
      override def getSolo: Boolean = false
    }
}
