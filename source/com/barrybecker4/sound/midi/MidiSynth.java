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

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    MidiRecordFrame recordFrame;


    /** Builds the piano, controllers, and instrument table. */
    public MidiSynth() {
        model = new MidiSynthModel();
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        p.setBorder(new CompoundBorder(cb, eb));
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBorder(new EmptyBorder(10, 20, 10, 5));
        pp.add(new PianoPanel(this));
        p.add(pp);
        p.add(new MidiControls(this));

        InstrumentsTable itable = new InstrumentsTable(model, this);
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
        lsm.setSelectionInterval(0, 0);
        lsm = table.getColumnModel().getSelectionModel();
        lsm.setSelectionInterval(0, 0);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
     * Launches the synthesizer in a frame (standalone demo).
     *
     * @param args unused
     */
    public static void main(String[] args) {
        final MidiSynth midiSynth = new MidiSynth();
        midiSynth.open();
        JFrame f = new JFrame("Midi Synthesizer");
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.getContentPane().add("Center", midiSynth);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 760;
        int h = 470;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.setVisible(true);
    }
}
