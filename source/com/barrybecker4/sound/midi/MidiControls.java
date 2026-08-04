package com.barrybecker4.sound.midi;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A collection of MIDI controllers for {@link MidiSynth}.
 */
class MidiControls extends JPanel implements ActionListener, ChangeListener, ItemListener {

    private final MidiSynth synth;

    /** Opens {@link MidiRecordFrame} when pressed. */
    private JButton recordB;

    /** Builds sliders, channel selector, checkboxes, and record control. */
    MidiControls(MidiSynth synth) {
        this.synth = synth;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        synth.veloS = createSlider("Velocity", p);
        synth.presS = createSlider("Pressure", p);
        synth.revbS = createSlider("Reverb", p);

        // create a slider with a 14-bit range of values for pitch-bend
        synth.bendS = create14BitSlider("Bend", p);

        p.add(Box.createHorizontalStrut(10));
        add(p);

        p = new JPanel();
        p.setBorder(new EmptyBorder(10, 0, 10, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        JComboBox<String> combo = new JComboBox<>();
        combo.setPreferredSize(new Dimension(120, 25));
        combo.setMaximumSize(new Dimension(120, 25));
        for (int i = 1; i <= 16; i++) {
            combo.addItem("Channel " + i);
        }
        combo.addItemListener(this);
        p.add(combo);
        p.add(Box.createHorizontalStrut(20));

        synth.muteCB = createCheckBox("Mute", p);
        synth.soloCB = createCheckBox("Solo", p);
        synth.monoCB = createCheckBox("Mono", p);

        createButton("All Notes Off", p);
        p.add(Box.createHorizontalStrut(10));
        p.add(synth.mouseOverCB);
        p.add(Box.createHorizontalStrut(10));
        recordB = createButton("Record...", p);
        add(p);
    }

    private JButton createButton(String name, JPanel p) {
        JButton b = new JButton(name);
        b.addActionListener(this);
        p.add(b);
        return b;
    }

    private JCheckBox createCheckBox(String name, JPanel p) {
        JCheckBox cb = new JCheckBox(name);
        cb.addItemListener(this);
        p.add(cb);
        return cb;
    }

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

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider) e.getSource();
        int value = slider.getValue();
        TitledBorder tb = (TitledBorder) slider.getBorder();
        String s = tb.getTitle();
        tb.setTitle(s.substring(0, s.indexOf('=') + 1) + value);
        ChannelData cc = synth.cc;
        if (s.startsWith("Velocity")) {
            cc.velocity = value;
        } else if (s.startsWith("Pressure")) {
            cc.channel.setChannelPressure(cc.pressure = value);
        } else if (s.startsWith("Bend")) {
            cc.channel.setPitchBend(cc.bend = value);
        } else if (s.startsWith("Reverb")) {
            cc.channel.controlChange(MidiSynth.REVERB, cc.reverb = value);
        }
        slider.repaint();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) e.getSource();
            synth.cc = synth.channels[combo.getSelectedIndex()];
            setComponentStates(synth.cc);
        } else {
            JCheckBox cb = (JCheckBox) e.getSource();
            String name = cb.getText();
            ChannelData cc = synth.cc;
            if (name.startsWith("Mute")) {
                cc.channel.setMute(cc.mute = cb.isSelected());
            } else if (name.startsWith("Solo")) {
                cc.channel.setSolo(cc.solo = cb.isSelected());
            } else if (name.startsWith("Mono")) {
                cc.channel.setMono(cc.mono = cb.isSelected());
            } else if (name.startsWith("Sustain")) {
                cc.sustain = cb.isSelected();
                cc.channel.controlChange(MidiSynth.SUSTAIN, cc.sustain ? 127 : 0);
            }
        }
    }

    /** Syncs sliders and checkboxes to the given channel snapshot. */
    private void setComponentStates(ChannelData cc) {
        synth.table.setRowSelectionInterval(cc.row, cc.row);
        synth.table.setColumnSelectionInterval(cc.col, cc.col);

        synth.soloCB.setSelected(cc.solo);
        synth.monoCB.setSelected(cc.mono);
        synth.muteCB.setSelected(cc.mute);

        JSlider[] slider = {synth.veloS, synth.presS, synth.bendS, synth.revbS};
        int[] v = {cc.velocity, cc.pressure, cc.bend, cc.reverb};
        for (int i = 0; i < slider.length; i++) {
            TitledBorder tb = (TitledBorder) slider[i].getBorder();
            String s = tb.getTitle();
            tb.setTitle(s.substring(0, s.indexOf('=') + 1) + v[i]);
            slider[i].repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (button.getText().startsWith("All")) {
            for (ChannelData channel : synth.channels) {
                channel.channel.allNotesOff();
            }
            for (MidiKey key : synth.keys) {
                key.setNoteState(MidiSynth.OFF);
            }
        } else if (button.getText().startsWith("Record")) {
            if (synth.recordFrame != null) {
                synth.recordFrame.toFront();
            } else {
                synth.recordFrame = new MidiRecordFrame(synth);
            }
        }
    }
}
