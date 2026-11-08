package com.barrybecker4.sound.midi

import javax.sound.midi.*

/** MIDI synthesizer and sequencer state for [[MidiSynth]]. */
class MidiSynthModel private[midi] {

  private var sequencer: Sequencer = null
  private var sequence: Sequence = null
  private var synthesizer: Synthesizer = null
  private var instruments: Array[Instrument] = null

  private[midi] def getMidiChannels: Array[MidiChannel] = synthesizer.getChannels

  private[midi] def getResolution: Int = sequence.getResolution

  private[midi] def hasInstruments: Boolean = instruments != null

  private[midi] def getInstrument(i: Int): Instrument = instruments(i)

  private[midi] def loadInstrument(program: Int): Unit =
    synthesizer.loadInstrument(getInstrument(program))

  private[midi] def getSequencer: Sequencer = sequencer

  private[midi] def getSequence: Sequence = sequence

  private[midi] def addSequence(listener: MetaEventListener): Unit = {
    sequencer.addMetaEventListener(listener)
    try sequence = new Sequence(Sequence.PPQ, 10)
    catch case ex: Exception => ex.printStackTrace()
  }

  private[midi] def open(): Unit = {
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
