package com.barrybecker4.sound

import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.{CountDownLatch, TimeUnit}
import javax.sound.midi.MidiChannel
import scala.collection.mutable.ListBuffer

class MusicMakerPlayNoteSuite extends AnyFunSuite {

  test("playNote sends noteOff after a normal sleep") {
    val ons = ListBuffer.empty[Int]
    val offs = ListBuffer.empty[Int]
    val music = new MusicMaker(Array(fakeChannel(
      onNoteOn = ons += _,
      onNoteOff = offs += _
    )))

    music.playNote(60, duration = 10, velocity = 80)

    assertResult(Seq(60))(ons.toSeq)
    assertResult(Seq(60))(offs.toSeq)
  }

  test("interrupted playNote still sends noteOff and restores interrupt") {
    val ons = ListBuffer.empty[Int]
    val offs = ListBuffer.empty[Int]
    val onLatch = new CountDownLatch(1)
    val music = new MusicMaker(Array(fakeChannel(
      onNoteOn = n => {
        ons += n
        onLatch.countDown()
      },
      onNoteOff = offs += _
    )))

    val t = new Thread(() => music.playNote(60, duration = 5_000, velocity = 80))
    t.start()
    assert(onLatch.await(1, TimeUnit.SECONDS), "noteOn was never called")
    t.interrupt()
    t.join(1_000)

    assert(!t.isAlive)
    assert(t.isInterrupted)
    assertResult(Seq(60))(ons.toSeq)
    assertResult(Seq(60))(offs.toSeq)
  }

  private def fakeChannel(
      onNoteOn: Int => Unit,
      onNoteOff: Int => Unit
  ): MidiChannel =
    new MidiChannel {
      override def noteOn(noteNumber: Int, velocity: Int): Unit = onNoteOn(noteNumber)
      override def noteOff(noteNumber: Int, velocity: Int): Unit = onNoteOff(noteNumber)
      override def noteOff(noteNumber: Int): Unit = onNoteOff(noteNumber)
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
