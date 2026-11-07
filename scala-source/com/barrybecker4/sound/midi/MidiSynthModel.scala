package com.barrybecker4.sound.midi

import javax.sound.midi.*

/** MIDI synthesizer and sequencer state for [[MidiSynth]]. */
class MidiSynthModel {

  private var sequencer: Sequencer = null
  private var sequence: Sequence = null
  private var synthesizer: Synthesizer = null
  private var instruments: Array[Instrument] = null

  def getMidiChannels: Array[MidiChannel] = synthesizer.getChannels

  def getResolution: Int = sequence.getResolution

  def hasInstruments: Boolean = instruments != null

  def getInstrument(i: Int): Instrument = instruments(i)

  def loadInstrument(program: Int): Unit =
    synthesizer.loadInstrument(getInstrument(program))

  def getSequencer: Sequencer = sequencer

  def getSequence: Sequence = sequence

  def addSequence(listener: MetaEventListener): Unit = {
    sequencer.addMetaEventListener(listener)
    try sequence = new Sequence(Sequence.PPQ, 10)
    catch case ex: Exception => ex.printStackTrace()
  }

  def open(): Unit = {
    try
      if (synthesizer == null) {
        synthesizer = MidiSystem.getSynthesizer
        if (synthesizer == null) {
          System.out.println("getSynthesizer() failed!")
          return
        }
      }
      synthesizer.open()
      sequencer = MidiSystem.getSequencer
      sequence = new Sequence(Sequence.PPQ, 10)
    catch
      case ex: Exception =>
        ex.printStackTrace()
        return

    val sb = synthesizer.getDefaultSoundbank
    if (sb != null) {
      instruments = sb.getInstruments
      synthesizer.loadInstrument(instruments(0))
    }
  }
}
