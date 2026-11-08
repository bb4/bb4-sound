package com.barrybecker4.sound.midi

import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import java.awt.{Font, Toolkit}
import java.io.{File, IOException}
import javax.sound.midi.{MetaEventListener, MetaMessage, MidiSystem, Sequence, Sequencer}
import javax.swing._
import javax.swing.border.{CompoundBorder, EmptyBorder, EtchedBorder}
import javax.swing.event.TableModelEvent
import javax.swing.table.AbstractTableModel
import scala.collection.mutable.ArrayBuffer

/** A frame that allows for MIDI capture and saving the captured data. */
private[midi] class MidiRecordFrame(synth: MidiSynth)
    extends JFrame("Midi Capture"),
      ActionListener,
      MetaEventListener {

  private var recordB: JButton = null
  private var playB: JButton = null
  private var saveB: JButton = null
  private val tracks = ArrayBuffer.empty[TrackData]
  private var dataModel: AbstractTableModel = null
  private var table: JTable = null

  {
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = synth.recordFrame = null
    })
    synth.model.addSequence(this)

    val north = new JPanel()
    north.setBorder(new EmptyBorder(5, 5, 5, 5))
    north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS))
    recordB = createButton("Record", north, enabled = true)
    playB = createButton("Play", north, enabled = false)
    saveB = createButton("Save...", north, enabled = false)
    getContentPane.add(north, "North")

    val columnNames = Array("Channel #", "Instrument")
    dataModel = buildTracksTableModel(columnNames)
    table = new JTable(dataModel)
    layoutTableScrollPane()
  }

  private def buildTracksTableModel(names: Array[String]): AbstractTableModel =
    new AbstractTableModel {
      override def getColumnCount: Int = names.length
      override def getRowCount: Int = tracks.size
      override def getValueAt(row: Int, col: Int): AnyRef =
        if (col == 0) tracks(row).chanNum
        else if (col == 1) tracks(row).name
        else null
      override def getColumnName(col: Int): String = names(col)
      override def getColumnClass(c: Int): Class[?] =
        if (c == 0) classOf[Integer] else if (c == 1) classOf[String] else classOf[Object]
      override def isCellEditable(row: Int, col: Int): Boolean = false
      override def setValueAt(value: AnyRef, row: Int, col: Int): Unit =
        if (col == 0) tracks(row).chanNum = value.asInstanceOf[Integer]
        else if (col == 1) tracks(row).name = value.asInstanceOf[String]
    }

  private def layoutTableScrollPane(): Unit = {
    table.getColumn("Channel #").setMaxWidth(65)
    table.sizeColumnsToFit(0)
    val scrollPane = new JScrollPane(table)
    scrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 5, 5), new EtchedBorder()))
    getContentPane.add(scrollPane, "Center")
    pack()
    val d = Toolkit.getDefaultToolkit.getScreenSize
    val w = 210
    val h = 160
    setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2)
    setSize(w, h)
    setVisible(true)
  }

  private def createButton(name: String, p: JPanel, enabled: Boolean): JButton = {
    val b = new JButton(name)
    b.setFont(new Font("Verdana", Font.PLAIN, 10))
    b.setEnabled(enabled)
    b.addActionListener(this)
    p.add(b)
    b
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    val button = e.getSource.asInstanceOf[JButton]
    if (button eq recordB) handleRecordToggle()
    else if (button eq playB) handlePlayToggle()
    else if (button eq saveB) handleSaveRequest()
  }

  private def handleRecordToggle(): Unit = {
    synth.record = recordB.getText.startsWith("Record")
    if (synth.record) startRecordingSession() else stopRecordingSession()
  }

  private def startRecordingSession(): Unit = {
    val cc = synth.cc
    synth.track = synth.model.getSequence.createTrack()
    synth.startTime = System.currentTimeMillis()
    synth.createShortEvent(MidiSynth.PROGRAM, cc.col * InstrumentsTable.NUM_ROWS + cc.row)
    recordB.setText("Stop")
    playB.setEnabled(false)
    saveB.setEnabled(false)
  }

  private def stopRecordingSession(): Unit = {
    val cc = synth.cc
    val name =
      if (synth.model.hasInstruments)
        synth.model.getInstrument(cc.col * InstrumentsTable.NUM_ROWS + cc.row).getName
      else Integer.toString(cc.col * InstrumentsTable.NUM_ROWS + cc.row)
    tracks += new TrackData(cc.num + 1, name, synth.track)
    table.tableChanged(new TableModelEvent(dataModel))
    recordB.setText("Record")
    playB.setEnabled(true)
    saveB.setEnabled(true)
  }

  private def handlePlayToggle(): Unit =
    if (playB.getText.startsWith("Play")) startPlayback() else stopPlayback()

  private def startPlayback(): Unit = {
    try MidiRecordFrame.preparePlayback(synth.model.getSequencer, synth.model.getSequence)
    catch case ex: Exception => ex.printStackTrace()
    synth.model.getSequencer.start()
    playB.setText("Stop")
    recordB.setEnabled(false)
  }

  private def stopPlayback(): Unit = {
    synth.model.getSequencer.stop()
    playB.setText("Play")
    recordB.setEnabled(true)
  }

  private def handleSaveRequest(): Unit = {
    try {
      val fc = new JFileChooser(new File(System.getProperty("user.dir")))
      fc.setFileFilter(new javax.swing.filechooser.FileFilter {
        override def accept(f: File): Boolean = f.isDirectory
        override def getDescription: String = "Save as .mid file."
      })
      if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) saveMidiFile(fc.getSelectedFile)
    } catch {
      case ex: SecurityException => ex.printStackTrace()
    }
  }

  override def meta(message: MetaMessage): Unit = {
    if (message.getType == 47) {
      playB.setText("Play")
      recordB.setEnabled(true)
    }
  }

  def saveMidiFile(file: File): Unit = {
    try {
      val fileTypes = MidiSystem.getMidiFileTypes(synth.model.getSequence)
      if (fileTypes.isEmpty) System.out.println("Can't save sequence")
      else if (MidiSystem.write(synth.model.getSequence, fileTypes(0), file) == -1) {
        throw new IOException("Problems writing to file")
      }
    } catch {
      case ex: Exception => ex.printStackTrace()
    }
  }
}

private[midi] object MidiRecordFrame {

  /** Open the sequencer, load `sequence`, and rewind so Play always starts from the beginning. */
  private[midi] def preparePlayback(sequencer: Sequencer, sequence: Sequence): Unit = {
    sequencer.open()
    sequencer.setSequence(sequence)
    sequencer.setTickPosition(0)
  }
}
