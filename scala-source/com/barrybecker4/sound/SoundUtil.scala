package com.barrybecker4.sound

import com.barrybecker4.common.util.FileUtil

import java.net.URL
import java.util.concurrent.Executors
import javax.sound.sampled.*

/**
 * Static methods for playing sound files.
 *
 * @author Barry Becker
 */
object SoundUtil {

  private val PLAYBACK = Executors.newCachedThreadPool(r => {
    val t = new Thread(r, "bb4-sound-playback")
    t.setDaemon(true)
    t
  })

  def playSoundAndWait(soundPath: String): Unit =
    playSoundInternal(FileUtil.getURL(soundPath))

  def playSound(soundPath: String): Unit =
    playAsync(() => playSoundInternal(FileUtil.getURL(soundPath)))

  def playSound(clipURL: URL): Unit =
    playAsync(() => playSoundInternal(clipURL))

  private def playAsync(task: Runnable): Unit = PLAYBACK.execute(task)

  private def playSoundInternal(clipURL: URL): Unit = {
    try {
      val fis = AudioSystem.getAudioInputStream(clipURL)
      try {
        val ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, fis)
        try {
          val af = ais.getFormat
          val line = AudioSystem.getSourceDataLine(af)
          try {
            line.open(af)
            val bufSize = line.getBufferSize
            line.start()
            val data = new Array[Byte](bufSize)
            var bytesRead = 0
            while {
              bytesRead = ais.read(data, 0, data.length)
              bytesRead != -1
            } do line.write(data, 0, bytesRead)
            line.drain()
            line.stop()
          } finally line.close()
        } finally ais.close()
      } finally fis.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
