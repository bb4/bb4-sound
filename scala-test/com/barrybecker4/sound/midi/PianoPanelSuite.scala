package com.barrybecker4.sound.midi

import org.scalatest.funsuite.AnyFunSuite

class PianoPanelSuite extends AnyFunSuite {

  test("black keys skip E-F and B-C") {
    val keys = PianoPanel.blackKeyLeftEdges(octaves = 6, kw = 16, transpose = 24)
    val xs = keys.map(_.x)
    assertResult(Seq(12, 28, 60, 76, 92))(xs.take(5))
    assertResult(124)(xs(5)) // octave 1 C#
    assertResult(172)(xs(7)) // octave 1 F#
    assertResult(Seq(25, 27, 30, 32, 34, 37))(keys.take(6).map(_.note))
    assertResult(42)(keys(7).note) // octave 1 F#
  }

  test("black-key gaps repeat at every octave boundary") {
    val kw = 16
    val transpose = 24
    val keys = PianoPanel.blackKeyLeftEdges(octaves = 6, kw = kw, transpose = transpose)
    val withinOctave = Seq(1, 2, 4, 5, 6).map(whiteKeys => whiteKeys * kw - 4)
    val expectedX = (0 until 6).flatMap(octave => withinOctave.map(_ + octave * 7 * kw))
    assertResult(expectedX)(keys.map(_.x))

    val accidentals = Seq(1, 3, 6, 8, 10)
    val expectedNotes = (0 until 6).flatMap(octave => accidentals.map(_ + octave * 12 + transpose))
    assertResult(expectedNotes)(keys.map(_.note))
    assert(keys.forall(key => key.y == 0 && key.width == kw / 2 && key.height == 40))
  }

  test("black-key rectangles cover sharps and leave the E-F and B-C gaps") {
    val keys = PianoPanel.blackKeyLeftEdges(octaves = 6, kw = 16, transpose = 24)

    def notesAt(px: Int, py: Int = 10): Seq[Int] =
      keys.filter { key =>
        px >= key.x && px < key.x + key.width && py >= key.y && py < key.y + key.height
      }.map(_.note)

    assertResult(Seq(30))(notesAt(62)) // octave 0 F#, above the white keys
    assertResult(Seq.empty)(notesAt(48)) // E-F boundary
    assertResult(Seq.empty)(notesAt(112)) // B-C boundary into octave 1
    assertResult(Seq(37))(notesAt(126)) // octave 1 C#
    assertResult(Seq(42))(notesAt(174)) // octave 1 F#
    assertResult(Seq.empty)(notesAt(62, py = 50)) // below the black-key band
  }
}
