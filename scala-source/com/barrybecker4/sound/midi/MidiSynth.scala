package com.barrybecker4.sound.midi

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{Color, Dimension, Toolkit}
import javax.sound.midi.{MidiEvent, ShortMessage, Track}
import javax.swing.*
import javax.swing.border.{BevelBorder, CompoundBorder, EmptyBorder}
import scala.collection.mutable

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 *
 * @author Brian Lichtenwalter
 */
class MidiSynth extends JPanel(BorderLayout()), ProgramChangeHandler {

  val model = new MidiSynthModel
  var channels: Array[ChannelData] = null
  var cc: ChannelData = null

  val mouseOverCB = new JCheckBox("mouseOver", true)
  var veloS: JSlider = null
  var presS: JSlider = null
  var bendS: JSlider = null
  var revbS: JSlider = null
  var soloCB: JCheckBox = null
  var monoCB: JCheckBox = null
  var muteCB: JCheckBox = null
  var sustCB: JCheckBox = null

  val keys = mutable.ArrayBuffer.empty[MidiKey]
  val whiteKeys = mutable.ArrayBuffer.empty[MidiKey]
  var table: JTable = null

  var record = false
  var track: Track = null
  var startTime = 0L
  var recordFrame: MidiRecordFrame = null

  val jfcBlue = new Color(204, 204, 255)
  val pink = new Color(255, 175, 175)

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

  def createShortEvent(`type`: Int, num: Int): Unit = {
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
