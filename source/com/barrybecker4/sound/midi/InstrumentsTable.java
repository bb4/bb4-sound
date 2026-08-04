package com.barrybecker4.sound.midi;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Table for 128 general MIDI melody instruments.
 */
class InstrumentsTable extends JPanel {

    static final int NUM_ROWS = 8;

    private static final String[] NAMES = {
            "Piano", "Chromatic Perc.", "Organ", "Guitar",
            "Bass", "Strings", "Ensemble", "Brass",
            "Reed", "Pipe", "Synth Lead", "Synth Pad",
            "Synth Effects", "Ethnic", "Percussive", "Sound Effects"
    };

    private static final int NUM_COLS = NAMES.length;

    final JTable table;

    InstrumentsTable(MidiSynthModel model, ProgramChangeHandler pch) {
        setLayout(new BorderLayout());

        TableModel dataModel = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return NUM_COLS;
            }

            @Override
            public int getRowCount() {
                return NUM_ROWS;
            }

            @Override
            public Object getValueAt(int r, int c) {
                if (model.hasInstruments()) {
                    return model.getInstrument(c * NUM_ROWS + r).getName();
                }
                return Integer.toString(c * NUM_ROWS + r);
            }

            @Override
            public String getColumnName(int c) {
                return NAMES[c];
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public void setValueAt(Object obj, int r, int c) {
                // read-only table
            }
        };

        table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ListSelectionModel rowSelection = table.getSelectionModel();
        rowSelection.addListSelectionListener(e -> {
            ListSelectionModel sm = (ListSelectionModel) e.getSource();
            if (!sm.isSelectionEmpty()) {
                pch.newRow(sm.getMinSelectionIndex());
            }
        });

        ListSelectionModel colSelection = table.getColumnModel().getSelectionModel();
        colSelection.addListSelectionListener(e -> {
            ListSelectionModel sm = (ListSelectionModel) e.getSource();
            if (!sm.isSelectionEmpty()) {
                pch.newColumn(sm.getMinSelectionIndex());
            }
        });

        table.setPreferredScrollableViewportSize(new Dimension(NUM_COLS * 110, 200));
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(true);
        for (String name : NAMES) {
            TableColumn column = table.getColumn(name);
            column.setPreferredWidth(110);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane sp = new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(sp);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 170);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(800, 170);
    }
}
