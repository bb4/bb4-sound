/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech

import com.barrybecker4.common.concurrency.ThreadUtil
import com.barrybecker4.common.util.FileUtil

import java.net.URL
import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem}

object PhoneToken {
  private[speech] val RESOURCE_PATH = "com/barrybecker4/sound/allophones/"
  private[speech] val COMMA_PAUSE = 200
  private[speech] val PERIOD_PAUSE = 680
  val MAX_MERGE_COUNT: Int = 1000
  private[speech] val DELAY_BETWEEN_WORDS = 200

  def computeMergeCount(previousSound: Array[Byte], thisSound: Array[Byte], maxMergeThreshold: Int): Int =
    if (previousSound.length >= maxMergeThreshold && thisSound.length >= maxMergeThreshold) maxMergeThreshold
    else 0

  def mergeAllophonesInPlace(previousSound: Array[Byte], thisSound: Array[Byte], mergeCount: Int): Unit = {
    var i = 0
    while (i < mergeCount) {
      previousSound(previousSound.length - mergeCount + i) =
        ((previousSound(previousSound.length - mergeCount + i) + thisSound(i)) / 2).toByte
      i += 1
    }
  }

  def truncateAfterMerge(thisSound: Array[Byte], mergeCount: Int): Array[Byte] = {
    val newSound = new Array[Byte](thisSound.length - mergeCount)
    System.arraycopy(thisSound, mergeCount, newSound, 0, newSound.length)
    newSound
  }
}

class PhoneToken private[speech] (
  private val phoneToken: String,
  private val shared: SharedSampleLine
) {
  def this(phoneToken: String) =
    this(phoneToken, new SharedSampleLine(SampleLine.openSourceDataLine))

  def process(previousSound: Array[Byte]): Array[Byte] = {
    if (!pauseForPunctuation()) {
      val phoneFile = PhoneToken.RESOURCE_PATH + phoneToken + ".au"
      val thisSound = getSound(phoneFile)
      assert(thisSound.length > 0, "Invalid sound file: " + phoneFile)
      sayPhoneToken(previousSound, thisSound)
    } else previousSound
  }

  def drain(): Unit = {
    if (shared.hasLine) {
      shared.drainAndFlush()
      ThreadUtil.sleep(100)
    }
    shared.close()
    ThreadUtil.sleep(PhoneToken.DELAY_BETWEEN_WORDS)
  }

  def playSound(data: Array[Byte]): Unit = {
    if (data == null || isPunctuation) return
    val playback = shared.current
    if (playback == null) throw new IllegalArgumentException("Problem processing " + phoneToken)
    if (data.length > 0) {
      playback.write(data, 0, data.length)
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
        val playback = shared.acquire(format)
        readFirstSampleBuffer(stream, format, playback)
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

  private def readFirstSampleBuffer(
    stream: AudioInputStream,
    format: AudioFormat,
    playback: SampleLine
  ): Array[Byte] = {
    val frameSizeInBytes = format.getFrameSize
    val bufferLengthInFrames = playback.getBufferSize >> 3
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
}
