package com.barrybecker4.sound.midi

import org.scalatest.funsuite.AnyFunSuite

import javax.sound.midi.MidiChannel

class ChannelDataSuite extends AnyFunSuite {

  test("pitch bend default is the 14-bit center, not 64") {
    val cc = new ChannelData(fakeChannel(), 0)
    assertResult(8192)(cc.bend)
    assertResult(64)(cc.velocity)
    assertResult(64)(cc.pressure)
    assertResult(64)(cc.reverb)
  }

  private def fakeChannel(): MidiChannel =
    new MidiChannel {
      override def noteOn(noteNumber: Int, velocity: Int): Unit = ()
      override def noteOff(noteNumber: Int, velocity: Int): Unit = ()
      override def noteOff(noteNumber: Int): Unit = ()
      override def setPolyPressure(noteNumber: Int, pressure: Int): Unit = ()
      override def getPolyPressure(noteNumber: Int): Int = 0
      override def setChannelPressure(pressure: Int): Unit = ()
      override def getChannelPressure: Int = 0
      override def controlChange(controller: Int, value: Int): Unit = ()
      override def getController(controller: Int): Int = 0
      override def programChange(program: Int): Unit = ()
      override def programChange(bank: Int, program: Int): Unit = ()
      override def getProgram: Int = 0
      override def setPitchBend(bend: Int): Unit = ()
      override def getPitchBend: Int = 0
      override def resetAllControllers(): Unit = ()
      override def allNotesOff(): Unit = ()
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
