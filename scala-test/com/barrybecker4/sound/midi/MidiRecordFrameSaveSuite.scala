package com.barrybecker4.sound.midi

import java.io.File
import java.nio.file.Files

import org.scalatest.funsuite.AnyFunSuite

/** Headless coverage for MIDI save filter and extension helper — no display, synthesizer, or mixer. */
class MidiRecordFrameSaveSuite extends AnyFunSuite {

  private val filter = new MidFileFilter

  test("midi filter keeps directories and .mid files") {
    val dir = Files.createTempDirectory("midi-save-filter").toFile
    try {
      assert(filter.accept(new File("a.mid")))
      assert(filter.accept(new File("a.MID")))
      assert(!filter.accept(new File("a.wav")))
      assert(filter.accept(dir))
    } finally {
      dir.delete()
    }
  }

  test("midi filter description matches the save label") {
    assertResult("Save as .mid file.")(filter.getDescription)
  }

  test("withMidExtension appends .mid when missing") {
    val file = new File("dir", "take1")
    val result = MidiRecordFrame.withMidExtension(file)
    assertResult("take1.mid")(result.getName)
    assertResult(file.getParentFile)(result.getParentFile)
  }

  test("withMidExtension leaves an existing .mid extension alone") {
    val lower = new File("dir", "take1.mid")
    val upper = new File("dir", "take1.MID")
    assert(MidiRecordFrame.withMidExtension(lower).eq(lower))
    assert(MidiRecordFrame.withMidExtension(upper).eq(upper))
  }

  test("withMidExtension works for a relative name with no parent") {
    val result = MidiRecordFrame.withMidExtension(new File("take1"))
    assertResult("take1.mid")(result.getName)
  }
}
