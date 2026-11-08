package com.barrybecker4.sound

import org.scalatest.funsuite.AnyFunSuite

import javax.sound.midi.MidiChannel

class MusicMakerStopSuite extends AnyFunSuite {

  test("stopAllSounds releases every channel, not only the last one") {
    val noteOns = Array.fill(16)(0)
    val noteOffs = Array.fill(16)(0)
    val channels = Array.tabulate(16)(i => fakeChannel(noteOns, noteOffs, i))
    val music = new MusicMaker(channels)

    music.startNote(0, 0, 60, channelIndex = 0, velocity = 100)
    music.startNote(0, 0, 67, channelIndex = 3, velocity = 100)

    assertResult(1)(noteOns(0))
    assertResult(1)(noteOns(3))
    assertResult(0)(noteOns(1))

    music.stopAllSounds()
    assertResult(List.fill(16)(1))(noteOffs.toList)
  }

  test("stopAllSounds skips null channel slots") {
    val noteOns = Array.fill(16)(0)
    val noteOffs = Array.fill(16)(0)
    val channels = Array.tabulate(16) { i =>
      if (i == 2 || i == 9) null
      else fakeChannel(noteOns, noteOffs, i)
    }
    val music = new MusicMaker(channels)

    music.startNote(0, 0, 60, channelIndex = 0, velocity = 100)
    music.startNote(0, 0, 64, channelIndex = 4, velocity = 80)
    music.stopAllSounds()

    val expected = List.tabulate(16)(i => if (i == 2 || i == 9) 0 else 1)
    assertResult(expected)(noteOffs.toList)
    assertResult(1)(noteOns(0))
    assertResult(1)(noteOns(4))
  }

  private def fakeChannel(noteOns: Array[Int], noteOffs: Array[Int], index: Int): MidiChannel =
    new MidiChannel {
      override def noteOn(noteNumber: Int, velocity: Int): Unit = noteOns(index) += 1
      override def allNotesOff(): Unit = noteOffs(index) += 1
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
