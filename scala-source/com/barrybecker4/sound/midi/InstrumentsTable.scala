package com.barrybecker4.sound.midi
/*
 * Extracted from the Sun Microsystems MidiSynth demo.
 * See MidiSynth.scala for the original copyright notice and license terms.
 */

import java.awt.{BorderLayout, Dimension}
import javax.swing.*
import javax.swing.ScrollPaneConstants
import javax.swing.table.AbstractTableModel

/** Table for 128 general MIDI melody instruments. */
private[midi] class InstrumentsTable(model: MidiSynthModel, pch: ProgramChangeHandler) extends JPanel(BorderLayout()) {

  val table: JTable = {
    val names = Array(
    "Piano", "Chromatic Perc.", "Organ", "Guitar",
    "Bass", "Strings", "Ensemble", "Brass",
    "Reed", "Pipe", "Synth Lead", "Synth Pad",
    "Synth Effects", "Ethnic", "Percussive", "Sound Effects"
    )
    val numCols = names.length

    val dataModel = new AbstractTableModel {
      override def getColumnCount: Int = numCols
      override def getRowCount: Int = InstrumentsTable.NUM_ROWS
      override def getValueAt(r: Int, c: Int): AnyRef =
        if (model.hasInstruments) model.getInstrument(c * InstrumentsTable.NUM_ROWS + r).getName
        else Integer.toString(c * InstrumentsTable.NUM_ROWS + r)
      override def getColumnName(c: Int): String = names(c)
      override def getColumnClass(c: Int): Class[?] = getValueAt(0, c).getClass
      override def isCellEditable(r: Int, c: Int): Boolean = false
      override def setValueAt(obj: AnyRef, r: Int, c: Int): Unit = ()
    }

    val tbl = new JTable(dataModel)
    tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    tbl.getSelectionModel.addListSelectionListener(e =>
      val sm = e.getSource.asInstanceOf[ListSelectionModel]
      if (!sm.isSelectionEmpty) pch.newRow(sm.getMinSelectionIndex)
    )

    tbl.getColumnModel.getSelectionModel.addListSelectionListener(e =>
      val sm = e.getSource.asInstanceOf[ListSelectionModel]
      if (!sm.isSelectionEmpty) pch.newColumn(sm.getMinSelectionIndex)
    )

    tbl.setPreferredScrollableViewportSize(new Dimension(numCols * 110, 200))
    tbl.setCellSelectionEnabled(true)
    tbl.setColumnSelectionAllowed(true)
    for (name <- names) tbl.getColumn(name).setPreferredWidth(110)
    tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)

    val sp = new JScrollPane(tbl)
    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
    add(sp)
    tbl
  }

  override def getPreferredSize: Dimension = new Dimension(800, 170)
  override def getMaximumSize: Dimension = new Dimension(800, 170)
}

private[midi] object InstrumentsTable {
  final val NUM_ROWS: Int = 8
}
