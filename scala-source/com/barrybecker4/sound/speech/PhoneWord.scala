/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech

import javax.sound.sampled.AudioFormat

import scala.jdk.CollectionConverters.*

/** Represents a single phonetic word. */
class PhoneWord private[speech] (
  phoneWord: String,
  lineFactory: AudioFormat => SampleLine = SampleLine.openSourceDataLine
) {

  def say(): Unit = {
    val shared = new SharedSampleLine(lineFactory)
    var previousSound: Array[Byte] = null
    var lastToken: PhoneToken = null
    try {
      for (t <- PhoneWord.splitAllophones(phoneWord).asScala) {
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

object PhoneWord {
  def apply(word: String): PhoneWord = new PhoneWord(word)

  /** Split a word into allophone tokens (delimiter `|`). Empty or null input yields an empty list. */
  def splitAllophones(word: String): java.util.List[String] =
    if (word == null || word.isEmpty) java.util.List.of()
    else java.util.Collections.unmodifiableList(word.split("\\|", -1).toList.asJava)
}
