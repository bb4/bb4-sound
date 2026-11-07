/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech

/** Makes the computer speak using speech synthesis. */
class SpeechSynthesizer {

  def sayText(text: String): Unit =
    sayPhoneWords(text.split(" "))

  def sayPhoneWords(words: Array[String]): Unit =
    words.foreach(word => PhoneWord(word).say())
}

object SpeechSynthesizer {
  def main(args: Array[String]): Unit = {
    val synthesizer = new SpeechSynthesizer
    if (args.length > 0) synthesizer.sayText(args(0))
    else synthesizer.sayText("y|ouu c|a|nn a|d aa s|i|n|ggg|ll w|o|r|dd p|ar|a|m|e|tt|er .|.")
    System.exit(0)
  }
}
