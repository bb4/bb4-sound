package com.barrybecker4.sound.midi;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Table for 128 general MIDI melody instruments.
 */
class InstrumentsTable extends JPanel {

    static int NUM_ROWS = 8;

    private String names[] = {
            "Piano", "Chromatic Perc.", "Organ", "Guitar",
            "Bass", "Strings", "Ensemble", "Brass",
            "Reed", "Pipe", "Synth Lead", "Synth Pad",
            "Synth Effects", "Ethnic", "Percussive", "Sound Effects" };

    private int nCols = names.length; // just show 128 instruments
    JTable table;

    InstrumentsTable(MidiSynthModel model, ProgramChangeHandler pch) {
        setLayout(new BorderLayout());

        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return nCols; }
            public int getRowCount() { return NUM_ROWS;}
            public Object getValueAt(int r, int c) {
                if (model.hasInstruments()) {
                    return model.getInstrument(c * NUM_ROWS + r).getName();
                } else {
                    return Integer.toString(c * NUM_ROWS + r);
                }
            }
            public String getColumnName(int c) {
                return names[c];
            }
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            public boolean isCellEditable(int r, int c) {return false;}
            public void setValueAt(Object obj, int r, int c) {}
        };

        table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Listener for row changes
        ListSelectionModel lsm = table.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    pch.newRow(sm.getMinSelectionIndex());
                }
            }
        });

        // Listener for column changes
        lsm = table.getColumnModel().getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    pch.newColumn(sm.getMinSelectionIndex());
                }
            }
        });

        table.setPreferredScrollableViewportSize(new Dimension(nCols * 110, 200));
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(true);
        for (String name : names) {
            TableColumn column = table.getColumn(name);
            column.setPreferredWidth(110);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane sp = new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(sp);
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 170);
    }
    public Dimension getMaximumSize() {
        return new Dimension(800, 170);
    }
}
