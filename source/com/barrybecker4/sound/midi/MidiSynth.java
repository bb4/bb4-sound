package com.barrybecker4.sound.midi;
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

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 * 1.15 99/12/03
 * @author Brian Lichtenwalter
 */
public class MidiSynth extends JPanel implements ProgramChangeHandler {

    /** MIDI status byte for program change messages ({@code 0xC0} base). */
    static final int PROGRAM = 192;

    /** MIDI control change number for sustain pedal. */
    static final int SUSTAIN = 64;
    /** MIDI control change number for reverb depth. */
    static final int REVERB = 91;

    /** MIDI status byte for note-on ({@code 0x90} base). */
    static final int NOTEON = 144;
    /** MIDI status byte for note-off ({@code 0x80} base). */
    static final int NOTEOFF = 128;
    /** Key state constant: note held (pressed). */
    static final int ON = 0;
    /** Key state constant: note released. */
    static final int OFF = 1;

    /** Fill color for active white keys when not recording. */
    final Color jfcBlue = new Color(204, 204, 255);
    /** Fill color for active keys while recording. */
    final Color pink = new Color(255, 175, 175);
    /** Synthesizer and sequencer state. */
    final MidiSynthModel model;
    /** Per-MIDI-channel UI and controller state. */
    ChannelData[] channels;
    /** Currently selected channel. */
    ChannelData cc;

    /** When selected, moving the mouse over keys plays them without clicking. */
    final JCheckBox mouseOverCB = new JCheckBox("mouseOver", true);
    /** Sliders for velocity, channel pressure, pitch bend, and reverb. */
    JSlider veloS, presS, bendS, revbS;
    /** Channel mode toggles (solo, mono, mute; sustain reserved). */
    JCheckBox soloCB, monoCB, muteCB, sustCB;
    /** All piano keys, hit-testing order (black drawn on top of white). */
    final List<MidiKey> keys = new ArrayList<>();
    /** White keys only, for painting order. */
    final List<MidiKey> whiteKeys = new ArrayList<>();
    /** Instrument picker table. */
    JTable table;

    /** Whether a recording session is in progress. */
    boolean record;
    /** MIDI track being recorded. */
    Track track;
    /** Wall-clock millis when recording started (for event timestamps). */
    long startTime;
    /** Non-modal capture window, or {@code null} if closed. */
    RecordFrame recordFrame;


    /** Builds the piano, controllers, and instrument table. */
    public MidiSynth() {
        model = new MidiSynthModel();
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        EmptyBorder eb = new EmptyBorder(5,5,5,5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        p.setBorder(new CompoundBorder(cb,eb));
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBorder(new EmptyBorder(10,20,10,5));
        pp.add(new PianoPanel(this));
        p.add(pp);
        p.add(new Controls());

        InstrumentsTable itable = new InstrumentsTable(model,this);
        table = itable.table;
        p.add(itable);

        add(p);
    }

    /** Opens the default synthesizer and wires {@link #channels} to the UI. */
    public void open() {
        model.open();

        MidiChannel[] midiChannels = model.getMidiChannels();
        channels = new ChannelData[midiChannels.length];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new ChannelData(midiChannels[i], i);
        }
        cc = channels[0];

        ListSelectionModel lsm = table.getSelectionModel();
        lsm.setSelectionInterval(0,0);
        lsm = table.getColumnModel().getSelectionModel();
        lsm.setSelectionInterval(0,0);
    }

    /**
     * Appends a short MIDI message to the recording track.
     * Tick timing uses sequence resolution: e.g. at 120 BPM,
     * {@code ticks = elapsedMillis * resolution / 500}.
     *
     * @param type raw status byte category (e.g. note or program change)
     * @param num  first data byte (note number or program)
     */
    void createShortEvent(int type, int num) {
        ShortMessage message = new ShortMessage();
        try {
            long millis = System.currentTimeMillis() - startTime;
            long tick = millis * model.getResolution() / 500;
            message.setMessage(type + cc.num, num, cc.velocity);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /**
     * @param row selected instrument row in the table
     */
    public void newRow(int row) {
       cc.row = row;
       programChange(cc.col * InstrumentsTable.NUM_ROWS + cc.row);
    }

    /**
     * @param column selected instrument column in the table
     */
    public void newColumn(int column) {
       cc.col = column;
       programChange(cc.col * InstrumentsTable.NUM_ROWS + cc.row);
    }

    /**
     * @param program MIDI program number (0-based instrument index)
     */
    public void programChange(int program) {
        if (model.hasInstruments()) {
            model.loadInstrument(program);
        }
        cc.channel.programChange(program);
        if (record) {
            createShortEvent(PROGRAM, program);
        }
    }

    /**
     * A collection of MIDI controllers.
     */
    class Controls extends JPanel implements ActionListener, ChangeListener, ItemListener {

        /** Opens {@link RecordFrame} when pressed. */
        JButton recordB;

        /** Builds sliders, channel selector, checkboxes, and record control. */
        Controls() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5,10,5,10));

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            veloS = createSlider("Velocity", p);
            presS = createSlider("Pressure", p);
            revbS = createSlider("Reverb", p);

            // create a slider with a 14-bit range of values for pitch-bend
            bendS = create14BitSlider("Bend", p);

            p.add(Box.createHorizontalStrut(10));
            add(p);

            p = new JPanel();
            p.setBorder(new EmptyBorder(10,0,10,0));
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            JComboBox<String> combo = new JComboBox<>();
            combo.setPreferredSize(new Dimension(120,25));
            combo.setMaximumSize(new Dimension(120,25));
            for (int i = 1; i <= 16; i++) {
                combo.addItem("Channel " + String.valueOf(i));
            }
            combo.addItemListener(this);
            p.add(combo);
            p.add(Box.createHorizontalStrut(20));

            muteCB = createCheckBox("Mute", p);
            soloCB = createCheckBox("Solo", p);
            monoCB = createCheckBox("Mono", p);
            // sustCB = createCheckBox("Sustain", p);

            createButton("All Notes Off", p);
            p.add(Box.createHorizontalStrut(10));
            p.add(mouseOverCB);
            p.add(Box.createHorizontalStrut(10));
            recordB = createButton("Record...", p);
            add(p);
        }

        /**
         * @param name button label
         * @param p panel to add the button to
         * @return the new button
         */
        JButton createButton(String name, JPanel p) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            p.add(b);
            return b;
        }

        /**
         * @param name checkbox label
         * @param p panel to add the checkbox to
         * @return the new checkbox
         */
        private JCheckBox createCheckBox(String name, JPanel p) {
            JCheckBox cb = new JCheckBox(name);
            cb.addItemListener(this);
            p.add(cb);
            return cb;
        }

        /**
         * @param name slider title prefix (0–127 range)
         * @param p panel to add the slider to
         * @return the new slider
         */
        private JSlider createSlider(String name, JPanel p) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 127, 64);
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = 64");
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }

        /**
         * @param name slider title prefix (14-bit pitch bend range)
         * @param p panel to add the slider to
         * @return the new slider
         */
        private JSlider create14BitSlider(String name, JPanel p) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 16383, 8192);
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = 8192");
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }

        /** {@inheritDoc} */
        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int value = slider.getValue();
            TitledBorder tb = (TitledBorder) slider.getBorder();
            String s = tb.getTitle();
            tb.setTitle(s.substring(0, s.indexOf('=')+1) + String.valueOf(value));
            if (s.startsWith("Velocity")) {
                cc.velocity = value;
            } else if (s.startsWith("Pressure")) {
                cc.channel.setChannelPressure(cc.pressure = value);
            } else if (s.startsWith("Bend")) {
                cc.channel.setPitchBend(cc.bend = value);
            } else if (s.startsWith("Reverb")) {
                cc.channel.controlChange(REVERB, cc.reverb = value);
            }
            slider.repaint();
        }

        /** {@inheritDoc} */
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                cc = channels[combo.getSelectedIndex()];
                setComponentStates(cc);
            } else {
                JCheckBox cb = (JCheckBox) e.getSource();
                String name = cb.getText();
                if (name.startsWith("Mute")) {
                    cc.channel.setMute(cc.mute = cb.isSelected());
                } else if (name.startsWith("Solo")) {
                    cc.channel.setSolo(cc.solo = cb.isSelected());
                } else if (name.startsWith("Mono")) {
                    cc.channel.setMono(cc.mono = cb.isSelected());
                } else if (name.startsWith("Sustain")) {
                    cc.sustain = cb.isSelected();
                    cc.channel.controlChange(SUSTAIN, cc.sustain ? 127 : 0);
                }
            }
        }

        /** Syncs sliders and checkboxes to the given channel snapshot. */
        private void setComponentStates(ChannelData cc) {
            table.setRowSelectionInterval(cc.row, cc.row);
            table.setColumnSelectionInterval(cc.col, cc.col);

            soloCB.setSelected(cc.solo);
            monoCB.setSelected(cc.mono);
            muteCB.setSelected(cc.mute);
            //sustCB.setSelected(sustain);

            JSlider[] slider = { veloS, presS, bendS, revbS };
            int[] v = { cc.velocity, cc.pressure, cc.bend, cc.reverb };
            for (int i = 0; i < slider.length; i++) {
                TitledBorder tb = (TitledBorder) slider[i].getBorder();
                String s = tb.getTitle();
                tb.setTitle(s.substring(0, s.indexOf('=')+1) + String.valueOf(v[i]));
                slider[i].repaint();
            }
        }

        /** {@inheritDoc} */
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.getText().startsWith("All")) {
                for (ChannelData channel : channels) {
                    channel.channel.allNotesOff();
                }
                for (MidiKey key : keys) {
                    key.setNoteState(OFF);
                }
            } else if (button.getText().startsWith("Record")) {
                if (recordFrame != null) {
                    recordFrame.toFront();
                } else {
                    recordFrame = new RecordFrame();
                }
            }
        }
    }

    /**
     * A frame that allows for midi capture &amp; saving the captured data.
     */
    class RecordFrame extends JFrame implements ActionListener, MetaEventListener {

        /** Start/stop recording, play/stop playback, save to file. */
        JButton recordB, playB, saveB;
        /** One entry per completed recording pass. */
        List<TrackData> tracks = new ArrayList<>();
        /** Backing model for {@link #table}. */
        TableModel dataModel;
        /** Lists recorded tracks (channel and instrument name). */
        JTable table;

        /** Creates the toolbar, table, and shows the window centered on screen. */
        RecordFrame() {
            super("Midi Capture");
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {recordFrame = null;}
            });

            model.addSequence(this);

            JPanel north = new JPanel();
            north.setBorder(new EmptyBorder(5,5,5,5));
            north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));

            recordB = createButton("Record", north, true);
            playB = createButton("Play", north, false);
            saveB = createButton("Save...", north, false);

            getContentPane().add("North", north);

            final String[] columnNames = { "Channel #", "Instrument" };
            dataModel = buildTracksTableModel(columnNames);
            table = new JTable(dataModel);
            layoutTableScrollPane();
        }

        /**
         * @param names column titles
         * @return table model over {@link #tracks}
         */
        private TableModel buildTracksTableModel(final String[] names) {
            return new AbstractTableModel() {
                public int getColumnCount() { return names.length; }
                public int getRowCount() { return tracks.size();}
                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        return (tracks.get(row)).chanNum;
                    } else if (col == 1) {
                        return (tracks.get(row)).name;
                    }
                    return null;
                }
                public String getColumnName(int col) {return names[col]; }
                public Class<?> getColumnClass(int c) {
                    if (c == 0) {
                        return Integer.class;
                    }
                    if (c == 1) {
                        return String.class;
                    }
                    return Object.class;
                }
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
                public void setValueAt(Object val, int row, int col) {
                    if (col == 0) {
                        (tracks.get(row)).chanNum = (Integer) val;
                    } else if (col == 1) {
                        (tracks.get(row)).name = (String) val;
                    }
                }
            };
        }

        /** Adds the scrollable track table and sizes the frame. */
        private void layoutTableScrollPane() {
            TableColumn channelCol = table.getColumn("Channel #");
            channelCol.setMaxWidth(65);
            table.sizeColumnsToFit(0);

            JScrollPane scrollPane = new JScrollPane(table);
            EmptyBorder eb = new EmptyBorder(0,5,5,5);
            scrollPane.setBorder(new CompoundBorder(eb,new EtchedBorder()));

            getContentPane().add("Center", scrollPane);
            pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int w = 210;
            int h = 160;
            setLocation(d.width/2 - w/2, d.height/2 - h/2);
            setSize(w, h);
            setVisible(true);
        }


        /**
         * @param name button label
         * @param p parent panel
         * @param state initial enabled state
         * @return the new button
         */
        JButton createButton(String name, JPanel p, boolean state) {
            JButton b = new JButton(name);
            b.setFont(new Font("Verdana", Font.PLAIN, 10));
            b.setEnabled(state);
            b.addActionListener(this);
            p.add(b);
            return b;
        }

        /** {@inheritDoc} */
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

        /** Toggles between recording and not, updating UI and sequence state. */
        private void handleRecordToggle() {
            record = recordB.getText().startsWith("Record");
            if (record) {
                startRecordingSession();
            } else {
                stopRecordingSession();
            }
        }

        /** Begins a new MIDI track and timestamps from {@link MidiSynth#startTime}. */
        private void startRecordingSession() {
            track = model.getSequence().createTrack();
            startTime = System.currentTimeMillis();
            createShortEvent(PROGRAM,cc.col * InstrumentsTable.NUM_ROWS + cc.row);
            recordB.setText("Stop");
            playB.setEnabled(false);
            saveB.setEnabled(false);
        }

        /** Finalizes the current track and appends it to {@link #tracks}. */
        private void stopRecordingSession() {
            String name;
            if (model.hasInstruments()) {
                name = model.getInstrument(cc.col * InstrumentsTable.NUM_ROWS + cc.row).getName();
            } else {
                name = Integer.toString(cc.col * InstrumentsTable.NUM_ROWS + cc.row);
            }
            tracks.add(new TrackData(cc.num+1, name, track));
            table.tableChanged(new TableModelEvent(dataModel));
            recordB.setText("Record");
            playB.setEnabled(true);
            saveB.setEnabled(true);
        }

        /** Starts or stops the built-in sequencer playback. */
        private void handlePlayToggle() {
            if (playB.getText().startsWith("Play")) {
                startPlayback();
            } else {
                stopPlayback();
            }
        }

        /** Opens the sequencer, loads {@link MidiSynthModel#getSequence()}, and plays. */
        private void startPlayback() {
            try {
                model.getSequencer().open();
                model.getSequencer().setSequence(model.getSequence());
            } catch (Exception ex) { ex.printStackTrace(); }
            model.getSequencer().start();
            playB.setText("Stop");
            recordB.setEnabled(false);
        }

        /** Stops sequencer playback and resets button labels. */
        private void stopPlayback() {
            model.getSequencer().stop();
            playB.setText("Play");
            recordB.setEnabled(true);
        }

        /** Shows a save dialog and writes a type-0/1 MIDI file. */
        private void handleSaveRequest() {
            try {
                File file = new File(System.getProperty("user.dir"));
                JFileChooser fc = new JFileChooser(file);
                fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
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

        /** {@inheritDoc} */
        public void meta(MetaMessage message) {
            if (message.getType() == 47) {  // 47 is end of track
                playB.setText("Play");
                recordB.setEnabled(true);
            }
        }

        /**
         * @param file destination path (typically {@code .mid})
         */
        void saveMidiFile(File file) {
            try {
                int[] fileTypes = MidiSystem.getMidiFileTypes(model.getSequence());
                if (fileTypes.length == 0) {
                    System.out.println("Can't save sequence");
                } else {
                    if (MidiSystem.write(model.getSequence(), fileTypes[0], file) == -1) {
                        throw new IOException("Problems writing to file");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Launches the synthesizer in a frame (standalone demo).
     *
     * @param args unused
     */
    public static void main(String[] args) {
        final MidiSynth midiSynth = new MidiSynth();
        midiSynth.open();
        JFrame f = new JFrame("Midi Synthesizer");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        f.getContentPane().add("Center", midiSynth);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 760;
        int h = 470;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.setVisible(true);
    }
}
