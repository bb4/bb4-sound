package com.barrybecker4.sound.speech

import javax.sound.sampled.{AudioFormat, AudioSystem, DataLine, SourceDataLine}

/**
 * One playback line for a spoken word.
 * Production opens a SourceDataLine; tests can supply a fake and count open/close.
 */
private[speech] trait SampleLine {
  def write(data: Array[Byte], offset: Int, length: Int): Unit
  def drain(): Unit
  def flush(): Unit
  def close(): Unit
  def getBufferSize: Int
}

private[speech] object SampleLine {

  /** Open and start one SourceDataLine. The caller must close it. */
  def openSourceDataLine(format: AudioFormat): SampleLine = {
    val outInfo = new DataLine.Info(classOf[SourceDataLine], format)
    if (!AudioSystem.isLineSupported(outInfo))
      throw new Exception("Line matching " + outInfo + " not supported.")
    val dataLine = AudioSystem.getLine(outInfo).asInstanceOf[SourceDataLine]
    var started = false
    try {
      dataLine.open(format, 50000)
      dataLine.start()
      started = true
      new SourceSampleLine(dataLine)
    } finally if (!started) dataLine.close()
  }

  private final class SourceSampleLine(line: SourceDataLine) extends SampleLine {
    override def write(data: Array[Byte], offset: Int, length: Int): Unit =
      line.write(data, offset, length)

    override def drain(): Unit = line.drain()

    override def flush(): Unit = line.flush()

    override def close(): Unit =
      if (line.isOpen) {
        try line.stop()
        finally line.close()
      }

    override def getBufferSize: Int = line.getBufferSize
  }
}

/**
 * At most one line per word. The first allophone opens it; drain closes it.
 * A later token plays the buffer the previous token decoded, so the line cannot
 * live on the token that decoded the samples.
 */
private[speech] final class SharedSampleLine(factory: AudioFormat => SampleLine) {
  private var line: SampleLine = null
  private var closed = false

  def acquire(format: AudioFormat): SampleLine = {
    if (closed) throw new IllegalStateException("Sample line already closed")
    if (line == null) line = factory(format)
    line
  }

  def current: SampleLine = line

  def hasLine: Boolean = line != null && !closed

  def drainAndFlush(): Unit = {
    line.drain()
    line.flush()
  }

  def close(): Unit =
    if (line != null && !closed) {
      line.close()
      closed = true
    }
}
