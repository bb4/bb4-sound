package com.barrybecker4.sound.midi

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.Tag

object RequiresSoundbank extends Tag("RequiresSoundbank")

class MidiSynthModelSuite extends AnyFunSuite {

  test("open exposes channels when soundbank present", RequiresSoundbank) {
    val model = new MidiSynthModel
    model.open()
    assume(model.hasInstruments, "No default MIDI soundbank")
    assert(model.getMidiChannels != null)
    assert(model.getMidiChannels.length > 0)
  }
}
