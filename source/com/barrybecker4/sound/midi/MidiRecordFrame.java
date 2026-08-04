package com.barrybecker4.sound.midi;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A frame that allows for MIDI capture and saving the captured data.
 */
class MidiRecordFrame extends JFrame implements ActionListener, MetaEventListener {

    private final MidiSynth synth;

    /** Start/stop recording, play/stop playback, save to file. */
    private JButton recordB;
    private JButton playB;
    private JButton saveB;
    /** One entry per completed recording pass. */
    private final List<TrackData> tracks = new ArrayList<>();
    /** Backing model for {@link #table}. */
    private TableModel dataModel;
    /** Lists recorded tracks (channel and instrument name). */
    private JTable table;

    /** Creates the toolbar, table, and shows the window centered on screen. */
    MidiRecordFrame(MidiSynth synth) {
        super("Midi Capture");
        this.synth = synth;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synth.recordFrame = null;
            }
        });

        synth.model.addSequence(this);

        JPanel north = new JPanel();
        north.setBorder(new EmptyBorder(5, 5, 5, 5));
        north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));

        recordB = createButton("Record", north, true);
        playB = createButton("Play", north, false);
        saveB = createButton("Save...", north, false);

        getContentPane().add("North", north);

        final String[] columnNames = {"Channel #", "Instrument"};
        dataModel = buildTracksTableModel(columnNames);
        table = new JTable(dataModel);
        layoutTableScrollPane();
    }

    private TableModel buildTracksTableModel(final String[] names) {
        return new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return names.length;
            }

            @Override
            public int getRowCount() {
                return tracks.size();
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == 0) {
                    return tracks.get(row).chanNum;
                } else if (col == 1) {
                    return tracks.get(row).name;
                }
                return null;
            }

            @Override
            public String getColumnName(int col) {
                return names[col];
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0) {
                    return Integer.class;
                }
                if (c == 1) {
                    return String.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public void setValueAt(Object val, int row, int col) {
                if (col == 0) {
                    tracks.get(row).chanNum = (Integer) val;
                } else if (col == 1) {
                    tracks.get(row).name = (String) val;
                }
            }
        };
    }

    private void layoutTableScrollPane() {
        TableColumn channelCol = table.getColumn("Channel #");
        channelCol.setMaxWidth(65);
        table.sizeColumnsToFit(0);

        JScrollPane scrollPane = new JScrollPane(table);
        EmptyBorder eb = new EmptyBorder(0, 5, 5, 5);
        scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));

        getContentPane().add("Center", scrollPane);
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 210;
        int h = 160;
        setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2);
        setSize(w, h);
        setVisible(true);
    }

    private JButton createButton(String name, JPanel p, boolean state) {
        JButton b = new JButton(name);
        b.setFont(new Font("Verdana", Font.PLAIN, 10));
        b.setEnabled(state);
        b.addActionListener(this);
        p.add(b);
        return b;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (button.equals(recordB)) {
            handleRecordToggle();
        } else if (button.equals(playB)) {
            handlePlayToggle();
        } else if (button.equals(saveB)) {
            handleSaveRequest();
        }
    }

    private void handleRecordToggle() {
        synth.record = recordB.getText().startsWith("Record");
        if (synth.record) {
            startRecordingSession();
        } else {
            stopRecordingSession();
        }
    }

    private void startRecordingSession() {
        ChannelData cc = synth.cc;
        synth.track = synth.model.getSequence().createTrack();
        synth.startTime = System.currentTimeMillis();
        synth.createShortEvent(MidiSynth.PROGRAM, cc.col * InstrumentsTable.NUM_ROWS + cc.row);
        recordB.setText("Stop");
        playB.setEnabled(false);
        saveB.setEnabled(false);
    }

    private void stopRecordingSession() {
        ChannelData cc = synth.cc;
        String name;
        if (synth.model.hasInstruments()) {
            name = synth.model.getInstrument(cc.col * InstrumentsTable.NUM_ROWS + cc.row).getName();
        } else {
            name = Integer.toString(cc.col * InstrumentsTable.NUM_ROWS + cc.row);
        }
        tracks.add(new TrackData(cc.num + 1, name, synth.track));
        table.tableChanged(new TableModelEvent(dataModel));
        recordB.setText("Record");
        playB.setEnabled(true);
        saveB.setEnabled(true);
    }

    private void handlePlayToggle() {
        if (playB.getText().startsWith("Play")) {
            startPlayback();
        } else {
            stopPlayback();
        }
    }

    private void startPlayback() {
        try {
            synth.model.getSequencer().open();
            synth.model.getSequencer().setSequence(synth.model.getSequence());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        synth.model.getSequencer().start();
        playB.setText("Stop");
        recordB.setEnabled(false);
    }

    private void stopPlayback() {
        synth.model.getSequencer().stop();
        playB.setText("Play");
        recordB.setEnabled(true);
    }

    private void handleSaveRequest() {
        try {
            File file = new File(System.getProperty("user.dir"));
            JFileChooser fc = new JFileChooser(file);
            fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Save as .mid file.";
                }
            });
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                saveMidiFile(fc.getSelectedFile());
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void meta(MetaMessage message) {
        if (message.getType() == 47) {  // 47 is end of track
            playB.setText("Play");
            recordB.setEnabled(true);
        }
    }

    void saveMidiFile(File file) {
        try {
            int[] fileTypes = MidiSystem.getMidiFileTypes(synth.model.getSequence());
            if (fileTypes.length == 0) {
                System.out.println("Can't save sequence");
            } else {
                if (MidiSystem.write(synth.model.getSequence(), fileTypes[0], file) == -1) {
                    throw new IOException("Problems writing to file");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
