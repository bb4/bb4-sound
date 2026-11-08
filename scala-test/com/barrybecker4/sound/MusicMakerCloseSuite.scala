package com.barrybecker4.sound

import org.scalatest.funsuite.AnyFunSuite

import java.util
import javax.sound.midi._

class MusicMakerCloseSuite extends AnyFunSuite {

  test("close releases the synthesizer once and is idempotent") {
    val closeCount = Array(0)
    val music = new MusicMaker(fakeSynthesizer(closeCount))

    music.close()
    assertResult(1)(closeCount(0))

    music.close()
    assertResult(1)(closeCount(0))
  }

  private def fakeSynthesizer(closeCount: Array[Int]): Synthesizer =
    new Synthesizer {
      override def close(): Unit = closeCount(0) += 1
      override def open(): Unit = ()
      override def isOpen: Boolean = false
      override def getDeviceInfo: MidiDevice.Info = null
      override def getMicrosecondPosition: Long = -1L
      override def getMaxReceivers: Int = 0
      override def getMaxTransmitters: Int = 0
      override def getReceiver: Receiver = null
      override def getReceivers: util.List[Receiver] = util.List.of()
      override def getTransmitter: Transmitter = null
      override def getTransmitters: util.List[Transmitter] = util.List.of()
      override def getMaxPolyphony: Int = 0
      override def getLatency: Long = 0L
      override def getChannels: Array[MidiChannel] = Array.fill(16)(null)
      override def getVoiceStatus: Array[VoiceStatus] = Array.empty
      override def isSoundbankSupported(soundbank: Soundbank): Boolean = false
      override def loadInstrument(instrument: Instrument): Boolean = false
      override def unloadInstrument(instrument: Instrument): Unit = ()
      override def remapInstrument(from: Instrument, to: Instrument): Boolean = false
      override def getDefaultSoundbank: Soundbank = null
      override def getAvailableInstruments: Array[Instrument] = Array.empty
      override def getLoadedInstruments: Array[Instrument] = Array.empty
      override def loadAllInstruments(soundbank: Soundbank): Boolean = false
      override def unloadAllInstruments(soundbank: Soundbank): Unit = ()
      override def loadInstruments(soundbank: Soundbank, patchList: Array[Patch]): Boolean = false
      override def unloadInstruments(soundbank: Soundbank, patchList: Array[Patch]): Unit = ()
    }
}
