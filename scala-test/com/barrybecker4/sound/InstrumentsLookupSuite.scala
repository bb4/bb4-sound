package com.barrybecker4.sound

import org.scalatest.funsuite.AnyFunSuite

import javax.sound.midi.MidiSystem

class InstrumentsLookupSuite extends AnyFunSuite {

  private val skipMidi = sys.env.getOrElse("CI", "") == "true"

  if (skipMidi) {
    ignore("getInstrumentIndex finds existing instrument by name") {}
    ignore("getInstrumentIndex returns negative for missing name") {}
    ignore("getInstrumentName matches flat index layout") {}
  } else {
    test("getInstrumentIndex finds existing instrument by name") {
      val synth = MidiSystem.getSynthesizer
      synth.open()
      try {
        val sb = synth.getDefaultSoundbank
        assume(sb != null, "No default soundbank")
        val instruments = new Instruments(sb)
        val name = instruments.getInstrument(0).getName
        assert(instruments.getInstrumentIndex(name) >= 0)
      } finally synth.close()
    }

    test("getInstrumentIndex returns negative for missing name") {
      val synth = MidiSystem.getSynthesizer
      synth.open()
      try {
        val sb = synth.getDefaultSoundbank
        assume(sb != null, "No default soundbank")
        val instruments = new Instruments(sb)
        assert(instruments.getInstrumentIndex("___no_such_instrument___") < 0)
      } finally synth.close()
    }

    test("getInstrumentName matches flat index layout") {
      val synth = MidiSystem.getSynthesizer
      synth.open()
      try {
        val sb = synth.getDefaultSoundbank
        assume(sb != null, "No default soundbank")
        val instruments = new Instruments(sb)
        assume(instruments.getNumInstruments > 8, "Need at least one full row")
        val expected = instruments.getInstrument(9).getName
        assertResult(expected)(instruments.getInstrumentName(1, 1))
      } finally synth.close()
    }
  }
}
