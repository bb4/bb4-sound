/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech

import javax.sound.sampled.AudioFormat

/** Represents a single phonetic word. */
private[speech] class PhoneWord(
  phoneWord: String,
  lineFactory: AudioFormat => SampleLine = SampleLine.openSourceDataLine
) {

  def say(): Unit = {
    val shared = new SharedSampleLine(lineFactory)
    var previousSound: Array[Byte] = null
    var lastToken: PhoneToken = null
    try {
      for (t <- PhoneWord.splitAllophones(phoneWord)) {
        lastToken = new PhoneToken(t, shared)
        previousSound = lastToken.process(previousSound)
      }
      if (lastToken != null) {
        lastToken.playSound(previousSound)
        lastToken.drain()
      }
    } finally shared.close()
  }
}

private[speech] object PhoneWord {
  def apply(word: String): PhoneWord = new PhoneWord(word)

  /** Split a word into allophone tokens (delimiter `|`). Empty or null input yields an empty list. */
  def splitAllophones(word: String): List[String] =
    if (word == null || word.isEmpty) Nil
    else word.split("\\|", -1).toList
}
