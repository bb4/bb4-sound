package com.barrybecker4.sound.midi
/*
 * @(#)MidiSynth.java	1.15	99/12/03
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{BorderLayout, Color, Dimension, Toolkit}
import javax.sound.midi.{MidiEvent, ShortMessage, Track}
import javax.swing.*
import javax.swing.border.{BevelBorder, CompoundBorder, EmptyBorder}
import scala.collection.mutable

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 * 1.15 99/12/03
 * @author Brian Lichtenwalter
 */
class MidiSynth extends JPanel(BorderLayout()), ProgramChangeHandler {

  private[midi] val model = new MidiSynthModel
  private[midi] var channels: Array[ChannelData] = null
  private[midi] var cc: ChannelData = null

  private[midi] val mouseOverCB = new JCheckBox("mouseOver", true)
  private[midi] var veloS: JSlider = null
  private[midi] var presS: JSlider = null
  private[midi] var bendS: JSlider = null
  private[midi] var revbS: JSlider = null
  private[midi] var soloCB: JCheckBox = null
  private[midi] var monoCB: JCheckBox = null
  private[midi] var muteCB: JCheckBox = null
  private[midi] var sustCB: JCheckBox = null

  private[midi] val keys = mutable.ArrayBuffer.empty[MidiKey]
  private[midi] val whiteKeys = mutable.ArrayBuffer.empty[MidiKey]
  private[midi] var table: JTable = null

  private[midi] var record = false
  private[midi] var track: Track = null
  private[midi] var startTime = 0L
  private[midi] var recordFrame: MidiRecordFrame = null

  private[midi] val jfcBlue = new Color(204, 204, 255)
  private[midi] val pink = new Color(255, 175, 175)

  {
    val p = new JPanel()
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS))
    val eb = new EmptyBorder(5, 5, 5, 5)
    val bb = new BevelBorder(BevelBorder.LOWERED)
    val cb = new CompoundBorder(eb, bb)
    p.setBorder(new CompoundBorder(cb, eb))
    val pp = new JPanel(new BorderLayout())
    pp.setBorder(new EmptyBorder(10, 20, 10, 5))
    pp.add(new PianoPanel(this), BorderLayout.CENTER)
    p.add(pp)
    p.add(new MidiControls(this))

    val itable = new InstrumentsTable(model, this)
    table = itable.table
    p.add(itable)
    add(p)
  }

  def open(): Unit = {
    model.open()
    val midiChannels = model.getMidiChannels
    channels = Array.tabulate(midiChannels.length)(i => new ChannelData(midiChannels(i), i))
    cc = channels(0)

    var lsm = table.getSelectionModel
    lsm.setSelectionInterval(0, 0)
    lsm = table.getColumnModel.getSelectionModel
    lsm.setSelectionInterval(0, 0)
  }

  private[midi] def createShortEvent(`type`: Int, num: Int): Unit = {
    val message = new ShortMessage()
    try
      val millis = System.currentTimeMillis() - startTime
      val tick = millis * model.getResolution / 500
      message.setMessage(`type` + cc.num, num, cc.velocity)
      track.add(new MidiEvent(message, tick))
    catch case ex: Exception => ex.printStackTrace()
  }

  override def newRow(row: Int): Unit = {
    cc.row = row
    programChange(cc.col * InstrumentsTable.NUM_ROWS + cc.row)
  }

  override def newColumn(column: Int): Unit = {
    cc.col = column
    programChange(cc.col * InstrumentsTable.NUM_ROWS + cc.row)
  }

  override def programChange(program: Int): Unit = {
    if (model.hasInstruments) model.loadInstrument(program)
    cc.channel.programChange(program)
    if (record) createShortEvent(MidiSynth.PROGRAM, program)
  }
}

object MidiSynth {
  final val PROGRAM = 192
  final val SUSTAIN = 64
  final val REVERB = 91
  final val NOTEON = 144
  final val NOTEOFF = 128
  final val ON = 0
  final val OFF = 1

  def main(args: Array[String]): Unit = {
    val midiSynth = new MidiSynth()
    midiSynth.open()
    val f = new JFrame("Midi Synthesizer")
    f.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = System.exit(0)
    })
    f.getContentPane.add(midiSynth, BorderLayout.CENTER)
    f.pack()
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val w = 760
    val h = 470
    f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2)
    f.setSize(w, h)
    f.setVisible(true)
  }
}
