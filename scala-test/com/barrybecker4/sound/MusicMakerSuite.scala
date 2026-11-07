// Copyright by Barry G. Becker, 2012. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound

import com.barrybecker4.common.concurrency.ThreadUtil
import org.scalatest.funsuite.AnyFunSuite

class MusicMakerSuite extends AnyFunSuite {

  private val Duration = 2
  private val music = new MusicMaker
  private val instruments = music.getInstruments
  private val skipMidi = sys.env.getOrElse("CI", "") == "true"

  if (skipMidi) {
    ignore("num instruments") {}
    ignore("play instruments simultaneously") {}
    ignore("play all instruments") {}
  } else {
    test("num instruments") {
      assume(instruments != null, "No MIDI soundbank")
      assert(instruments.getNumInstruments > 0)
    }

    test("play instruments simultaneously") {
      assume(instruments != null, "No MIDI soundbank")
      val numInstruments = instruments.getNumInstruments
      for (i <- 0 until numInstruments) {
        music.startNote(i / 8, i % 8, 80, i % 16, 200)
        ThreadUtil.sleep(2 * Duration)
      }
      music.stopAllSounds()
    }

    test("play all instruments") {
      assume(instruments != null, "No MIDI soundbank")
      val numInstruments = instruments.getNumInstruments
      for (i <- 0 until numInstruments) {
        music.playNote(i / 8, i % 8, 50, 1, Duration, 200)
      }
    }
  }
}
