package com.barrybecker4.sound

import javax.sound.midi.*

/**
 * This class provides a facade to the Java Sound API.
 * You can use it to generate all kinds of musical effects.
 *
 * @author Barry Becker
 */
class MusicMaker private[sound] (injectedChannels: Array[MidiChannel]) {

  /** Opens the platform synthesizer when one is available. */
  def this() = this(null: Array[MidiChannel])

  private val pitchBendCenter = 8192

  private var synthesizer: Synthesizer = null
  private var instruments: Instruments = null
  private var midiChannels: Array[MidiChannel] = null
  private var channel: MidiChannel = null
  private val channelsInjected = injectedChannels != null

  if (channelsInjected) {
    midiChannels = injectedChannels
    channel = injectedChannels.find(_ != null).orNull
  } else {
    initSynthesizer()
  }

  private def initSynthesizer(): Unit = {
    if (!openSynthesizerIfAvailable()) return

    val sb = synthesizer.getDefaultSoundbank
    if (sb == null) {
      System.err.println("Error: no sound bank present on this system")
      closeSynthesizerQuietly()
      return
    }

    instruments = new Instruments(sb)
    synthesizer.loadInstrument(instruments.getInstrument(0))

    midiChannels = synthesizer.getChannels
    channel = midiChannels(0)
    configureChannel(channel)
  }

  private def openSynthesizerIfAvailable(): Boolean = {
    try {
      if (synthesizer == null) {
        synthesizer = MidiSystem.getSynthesizer
        if (synthesizer == null) {
          System.err.println("getSynthesizer() failed!")
          return false
        }
      }
      synthesizer.open()
      true
    } catch {
      case ex: MidiUnavailableException =>
        System.err.println("Midi Unavailable. No Synthesizer will be present.")
        ex.printStackTrace()
        false
    }
  }

  private def closeSynthesizerQuietly(): Unit = {
    if (synthesizer != null) {
      synthesizer.close()
      synthesizer = null
    }
  }

  private def configureChannel(ch: MidiChannel): Unit = {
    ch.resetAllControllers()
    ch.setChannelPressure(64)
    ch.setPitchBend(pitchBendCenter)
    ch.controlChange(91, 127) // reverb
    ch.setMute(false)
  }

  /** @return instruments from the default soundbank, or null if none could be loaded */
  def getInstruments: Instruments = instruments

  private def isReady: Boolean = channel != null && (instruments != null || channelsInjected)

  def startNote(
      instrumentType: Int,
      instrumentSubType: Int,
      note: Int,
      channelIndex: Int,
      velocity: Int
  ): Unit = {
    if (!isReady) return
    initChannel(instrumentType, instrumentSubType, channelIndex)
    startNote(note, velocity)
  }

  def startNote(instrument: String, note: Int, channelIndex: Int, velocity: Int): Unit = {
    if (!isReady) return
    val i = instruments.getInstrumentIndex(instrument)
    if (i >= 0) startNote(i / 8, i % 8, note, channelIndex, velocity)
  }

  def playNote(
      instrumentType: Int,
      instrumentSubType: Int,
      note: Int,
      channelIndex: Int,
      duration: Int,
      velocity: Int
  ): Unit = {
    assert(channelIndex < 16, "channel index must be 0-15")
    if (!isReady) return
    initChannel(instrumentType, instrumentSubType, channelIndex)
    playNote(note, duration, velocity)
  }

  def startNote(note: Int, velocity: Int): Unit = {
    if (!isReady) return
    channel.noteOn(note, velocity)
  }

  def stopNote(note: Int, velocity: Int): Unit = {
    if (!isReady) return
    channel.noteOff(note, velocity)
  }

  def playNote(note: Int, duration: Int, velocity: Int): Unit = {
    assert(note >= 0)
    var n = note
    if (n > 127) {
      System.out.println("note needs to be in the range 0-127 it is " + n)
      n = 127
    }
    var started = false
    try {
      if (!isReady) return
      channel.noteOn(n, velocity)
      started = true
      Thread.sleep(duration)
    } catch {
      case _: InterruptedException => Thread.currentThread().interrupt()
    } finally {
      if (started) channel.noteOff(n, velocity)
    }
  }

  def playNote(instrument: String, note: Int, channelIndex: Int, duration: Int, velocity: Int): Unit = {
    if (!isReady) return
    val i = instruments.getInstrumentIndex(instrument)
    if (i >= 0) {
      playNote(i / 8, i % 8, note, channelIndex, duration, velocity)
    }
  }

  protected def initChannel(instrumentType: Int, instrumentSubType: Int, channelIndex: Int): Unit = {
    if (!isReady) return
    val instrument = 8 * instrumentType + instrumentSubType

    if (synthesizer != null) {
      synthesizer.loadInstrument(instruments.getInstrument(instrument))
    }
    channel = midiChannels(channelIndex)
    channel.programChange(instrument)
  }

  def stopAllSounds(): Unit = {
    if (!isReady) return
    midiChannels.foreach(ch => if (ch != null) ch.allNotesOff())
  }
}
