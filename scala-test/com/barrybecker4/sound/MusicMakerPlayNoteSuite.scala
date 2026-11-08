package com.barrybecker4.sound

import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sound.midi.MidiChannel
import scala.collection.mutable.ListBuffer

class MusicMakerPlayNoteSuite extends AnyFunSuite {

  test("playNote sends noteOff after a normal sleep") {
    val noteOns = ListBuffer.empty[Int]
    val noteOffs = ListBuffer.empty[Int]
    val music = new MusicMaker(Array(fakeChannel(noteOns, noteOffs)))

    music.playNote(60, duration = 20, velocity = 80)

    assertResult(Seq(60))(noteOns.toSeq)
    assertResult(Seq(60))(noteOffs.toSeq)
  }

  test("interrupted playNote still sends noteOff and restores interrupt flag") {
    val noteOns = ListBuffer.empty[Int]
    val noteOffs = ListBuffer.empty[Int]
    val noteOnLatch = new CountDownLatch(1)
    val music = new MusicMaker(Array(fakeChannel(noteOns, noteOffs, Some(noteOnLatch))))
    val interruptedAfter = new AtomicInteger(0)

    val t = new Thread(() => {
      music.playNote(60, duration = 5_000, velocity = 80)
      if (Thread.currentThread().isInterrupted) interruptedAfter.set(1)
    })
    t.start()

    assert(noteOnLatch.await(1, TimeUnit.SECONDS), "noteOn should happen before interrupt")
    t.interrupt()
    t.join(1_000)
    assert(!t.isAlive, "playNote thread should finish after interrupt")

    assertResult(Seq(60))(noteOns.toSeq)
    assertResult(Seq(60))(noteOffs.toSeq)
    assertResult(1)(interruptedAfter.get)
  }

  private def fakeChannel(
      noteOns: ListBuffer[Int],
      noteOffs: ListBuffer[Int],
      noteOnLatch: Option[CountDownLatch] = None
  ): MidiChannel =
    new MidiChannel {
      override def noteOn(noteNumber: Int, velocity: Int): Unit = {
        noteOns += noteNumber
        noteOnLatch.foreach(_.countDown())
      }
      override def noteOff(noteNumber: Int, velocity: Int): Unit = noteOffs += noteNumber
      override def noteOff(noteNumber: Int): Unit = noteOffs += noteNumber
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
