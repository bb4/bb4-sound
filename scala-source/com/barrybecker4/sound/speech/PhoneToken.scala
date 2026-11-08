/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech

import com.barrybecker4.common.concurrency.ThreadUtil
import com.barrybecker4.common.util.FileUtil

import java.net.URL
import java.util.Locale
import javax.sound.sampled.*

object PhoneToken {
  private[speech] val RESOURCE_PATH = "com/barrybecker4/sound/allophones/"
  private[speech] val COMMA_PAUSE = 200
  private[speech] val PERIOD_PAUSE = 680
  val MAX_MERGE_COUNT: Int = 1000
  /** Converted allophones are 16-bit big-endian mono PCM. `MAX_MERGE_COUNT` is a whole number of these frames. */
  private val PcmFrameBytes = 2
  private[speech] val DELAY_BETWEEN_WORDS = 200

  /**
   * Underscore filenames that are byte-identical to the lowercase recording.
   * The case fold does not strip underscores; these spellings are accepted explicitly.
   */
  private val LegacyAliasStem: Map[String, String] = Map(
    "aa_" -> "aa",
    "ay_" -> "ay",
    "d_" -> "d",
    "gg_" -> "gg",
    "ggg_" -> "ggg",
    "k_" -> "k",
    "l_" -> "l",
    "ouu_" -> "ouu",
    "p_" -> "p",
    "t_" -> "t",
    "u_" -> "u",
    "w_" -> "w",
    "y_" -> "y"
  )

  def computeMergeCount(previousSound: Array[Byte], thisSound: Array[Byte], maxMergeThreshold: Int): Int =
    if (previousSound.length >= maxMergeThreshold && thisSound.length >= maxMergeThreshold) maxMergeThreshold
    else 0

  def mergeAllophonesInPlace(previousSound: Array[Byte], thisSound: Array[Byte], mergeCount: Int): Unit = {
    val aligned = frameAligned(mergeCount)
    var offset = 0
    while (offset < aligned) {
      val prevIndex = previousSound.length - aligned + offset
      val averaged = (readBigEndianSample(previousSound, prevIndex) + readBigEndianSample(thisSound, offset)) / 2
      writeBigEndianSample(previousSound, prevIndex, averaged)
      offset += PcmFrameBytes
    }
  }

  def truncateAfterMerge(thisSound: Array[Byte], mergeCount: Int): Array[Byte] = {
    val aligned = frameAligned(mergeCount)
    val newSound = new Array[Byte](thisSound.length - aligned)
    System.arraycopy(thisSound, aligned, newSound, 0, newSound.length)
    newSound
  }

  /** Whole 16-bit frames inside `mergeCount`. A trailing odd byte is left out of the crossfade. */
  private def frameAligned(mergeCount: Int): Int =
    if (mergeCount <= 0) 0 else mergeCount - mergeCount % PcmFrameBytes

  private def readBigEndianSample(data: Array[Byte], index: Int): Int = {
    val unsigned = ((data(index) & 0xff) << 8) | (data(index + 1) & 0xff)
    if (unsigned >= 0x8000) unsigned - 0x10000 else unsigned
  }

  private def writeBigEndianSample(data: Array[Byte], index: Int, sample: Int): Unit = {
    data(index) = ((sample >> 8) & 0xff).toByte
    data(index + 1) = (sample & 0xff).toByte
  }

  /**
   * Classpath path of the allophone for `token`.
   * The stem must match exactly, ignoring case, so `a` finds `a.au` and `AY` finds `ay.au`.
   * An underscore is part of the stem (`GG_` is not `gg`) except for the legacy aliases above.
   */
  private[speech] def allophoneResource(token: String): Option[String] = {
    if (token == null || token.isEmpty) None
    else {
      val folded = token.toLowerCase(Locale.ROOT)
      val stem = LegacyAliasStem.getOrElse(folded, folded)
      val loader = Option(Thread.currentThread.getContextClassLoader).getOrElse(classOf[PhoneToken].getClassLoader)
      val candidates = List(stem, stem.toUpperCase(Locale.ROOT)).distinct
      candidates.iterator.map(name => RESOURCE_PATH + name + ".au").find(path => loader.getResource(path) != null)
    }
  }
}

class PhoneToken(private val phoneToken: String) {

  private var line: SourceDataLine = null

  def process(previousSound: Array[Byte]): Array[Byte] = {
    if (!pauseForPunctuation()) {
      val phoneFile = PhoneToken.allophoneResource(phoneToken)
        .getOrElse(PhoneToken.RESOURCE_PATH + phoneToken + ".au")
      val thisSound = getSound(phoneFile)
      assert(thisSound.length > 0, "Invalid sound file: " + phoneFile)
      sayPhoneToken(previousSound, thisSound)
    } else previousSound
  }

  def drain(): Unit = {
    if (line != null) {
      line.drain()
      line.flush()
      ThreadUtil.sleep(100)
    }
    ThreadUtil.sleep(PhoneToken.DELAY_BETWEEN_WORDS)
  }

  def playSound(data: Array[Byte]): Unit = {
    if (data == null || isPunctuation) return
    if (line == null) throw new IllegalArgumentException("Problem processing " + phoneToken)
    if (data.length > 0) {
      line.write(data, 0, data.length)
      ThreadUtil.sleep(100)
    }
  }

  private def pauseForPunctuation(): Boolean = {
    if (isPunctuation) {
      if (phoneToken == ",") ThreadUtil.sleep(PhoneToken.COMMA_PAUSE)
      else ThreadUtil.sleep(PhoneToken.PERIOD_PAUSE)
      true
    } else false
  }

  private def isPunctuation: Boolean = phoneToken == "," || phoneToken == "."

  private def sayPhoneToken(previousSound: Array[Byte], thisSound: Array[Byte]): Array[Byte] = {
    if (previousSound != null) {
      val mergeCount = PhoneToken.computeMergeCount(previousSound, thisSound, PhoneToken.MAX_MERGE_COUNT)
      PhoneToken.mergeAllophonesInPlace(previousSound, thisSound, mergeCount)
      playSound(previousSound)
      PhoneToken.truncateAfterMerge(thisSound, mergeCount)
    } else thisSound
  }

  private def getSound(sPath: String): Array[Byte] = {
    try {
      val url = FileUtil.getURL(sPath)
      val stream = openPcmStream(url)
      try {
        val format = stream.getFormat
        if (line == null) line = createDataLine(format)
        readFirstSampleBuffer(stream, format)
      } finally {
        stream.close()
      }
    } catch {
      case e: Exception =>
        System.err.println("Failed to load allophone audio: " + sPath)
        e.printStackTrace()
        Array.emptyByteArray
    }
  }

  private def openPcmStream(url: URL): AudioInputStream = {
    val stream = AudioSystem.getAudioInputStream(url)
    val format = stream.getFormat
    if (format.getEncoding == AudioFormat.Encoding.ULAW || format.getEncoding == AudioFormat.Encoding.ALAW) {
      val tmpFormat = createAudioFormat(format)
      AudioSystem.getAudioInputStream(tmpFormat, stream)
    } else stream
  }

  private def readFirstSampleBuffer(stream: AudioInputStream, format: AudioFormat): Array[Byte] = {
    val frameSizeInBytes = format.getFrameSize
    val bufferLengthInFrames = line.getBufferSize >> 3
    val bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes
    val data = new Array[Byte](bufferLengthInBytes)
    val numBytesRead = stream.read(data)
    if (numBytesRead <= 0) Array.emptyByteArray
    else {
      val newData = new Array[Byte](numBytesRead)
      System.arraycopy(data, 0, newData, 0, numBytesRead)
      newData
    }
  }

  private def createAudioFormat(format: AudioFormat): AudioFormat =
    new AudioFormat(
      AudioFormat.Encoding.PCM_SIGNED,
      format.getSampleRate,
      format.getSampleSizeInBits << 1,
      format.getChannels,
      format.getFrameSize << 1,
      format.getFrameRate,
      true
    )

  private def createDataLine(format: AudioFormat): SourceDataLine = {
    val outInfo = new DataLine.Info(classOf[SourceDataLine], format)
    if (!AudioSystem.isLineSupported(outInfo)) {
      throw new Exception("Line matching " + outInfo + " not supported.")
    }
    val dataLine = AudioSystem.getLine(outInfo).asInstanceOf[SourceDataLine]
    dataLine.open(format, 50000)
    dataLine.start()
    dataLine
  }
}
