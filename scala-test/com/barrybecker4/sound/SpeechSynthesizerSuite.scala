// Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound

import com.barrybecker4.sound.speech.SpeechSynthesizer
import org.scalatest.funsuite.AnyFunSuite

class SpeechSynthesizerSuite extends AnyFunSuite {

  private val EnterNumber = "p|l|ee|z e|n|t|er aa nn|u|m|b|er .|."
  private val PlayGame = Array("w|u|d", "y|ouu", "l|ii|k", "t|ouu", "p|l|ay", "aa", "gg|AY|M")
  private val speech = new SpeechSynthesizer
  private val skipAudio = sys.env.getOrElse("CI", "") == "true"

  if (skipAudio) {
    ignore("say enter number") {}
    ignore("say greeting") {}
  } else {
    test("say enter number") {
      speech.sayText(EnterNumber)
    }
    test("say greeting") {
      speech.sayPhoneWords(PlayGame)
    }
  }
}
