package com.barrybecker4.sound.midi

import java.awt.event.{MouseEvent, MouseListener, MouseMotionAdapter}
import java.awt.{BorderLayout, Color, Dimension, Graphics, Graphics2D, Point}
import javax.swing.JPanel
import scala.collection.mutable.ArrayBuffer

/** Piano renders black and white keys and plays the notes for a MIDI channel. */
class PianoPanel(synth: MidiSynth) extends JPanel(BorderLayout()), MouseListener {

  private val blackKeys = ArrayBuffer.empty[MidiKey]
  private var prevKey: MidiKey = null
  private val kw = 16
  private val kh = 80

  {
    setPreferredSize(new Dimension(42 * kw, kh + 1))
    val transpose = 24
    val whiteIDs = Array(0, 2, 4, 5, 7, 9, 11)

    var x = 0
    for (i <- 0 until 6; j <- 0 until 7) {
      val keyNum = i * 12 + whiteIDs(j) + transpose
      synth.whiteKeys += new MidiKey(synth, x, 0, kw, kh, keyNum)
      x += kw
    }

    x = 0
    for (i <- 0 until 6) {
      x += kw
      val keyNum = i * 12 + transpose
      blackKeys += new MidiKey(synth, x - 4, 0, kw / 2, kh / 2, keyNum + 1)
      x += kw
      blackKeys += new MidiKey(synth, x - 4, 0, kw / 2, kh / 2, keyNum + 3)
      x += kw
      blackKeys += new MidiKey(synth, x - 4, 0, kw / 2, kh / 2, keyNum + 6)
      x += kw
      blackKeys += new MidiKey(synth, x - 4, 0, kw / 2, kh / 2, keyNum + 8)
      x += kw
      blackKeys += new MidiKey(synth, x - 4, 0, kw / 2, kh / 2, keyNum + 10)
    }

    synth.keys ++= blackKeys
    synth.keys ++= synth.whiteKeys

    addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent): Unit = {
        if (synth.mouseOverCB.isSelected) {
          val key = getKey(e.getPoint)
          if (prevKey != null && prevKey != key) prevKey.off()
          if (key != null && (prevKey == null || prevKey != key)) key.on()
          prevKey = key
          repaint()
        }
      }
    })
    addMouseListener(this)
  }

  override def mousePressed(e: MouseEvent): Unit = {
    prevKey = getKey(e.getPoint)
    if (prevKey != null) {
      prevKey.on()
      repaint()
    }
  }

  override def mouseReleased(e: MouseEvent): Unit = {
    if (prevKey != null) {
      prevKey.off()
      repaint()
    }
  }

  override def mouseExited(e: MouseEvent): Unit = {
    if (prevKey != null) {
      prevKey.off()
      repaint()
      prevKey = null
    }
  }

  override def mouseClicked(e: MouseEvent): Unit = ()
  override def mouseEntered(e: MouseEvent): Unit = ()

  def getKey(point: Point): MidiKey =
    synth.keys.find(_.contains(point)).orNull

  override def paint(g: Graphics): Unit = {
    val g2 = g.asInstanceOf[Graphics2D]
    val d = getSize
    g2.setBackground(getBackground)
    g2.clearRect(0, 0, d.width, d.height)
    g2.setColor(Color.white)
    g2.fillRect(0, 0, 42 * kw, kh)
    for (key <- synth.whiteKeys) {
      if (key.isNoteOn) {
        g2.setColor(if (synth.record) synth.pink else synth.jfcBlue)
        g2.fill(key)
      }
      g2.setColor(Color.black)
      g2.draw(key)
    }
    for (key <- blackKeys) {
      if (key.isNoteOn) {
        g2.setColor(if (synth.record) synth.pink else synth.jfcBlue)
        g2.fill(key)
        g2.setColor(Color.black)
        g2.draw(key)
      } else {
        g2.setColor(Color.black)
        g2.fill(key)
      }
    }
  }
}
