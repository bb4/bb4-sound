package com.barrybecker4.sound.midi;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Piano renders black &amp; white keys and plays the notes for a MIDI channel.
 */
class PianoPanel extends JPanel implements MouseListener {

    /** Black keys (shorter, drawn above white keys). */
    private final List<MidiKey> blackKeys = new ArrayList<>();
    /** Key under the mouse or last clicked; may be {@code null}. */
    private MidiKey prevKey;
    /** White key width and height in pixels. */
    private final int kw = 16;
    private final int kh = 80;

    private final MidiSynth synth;

    /** Lays out keys for just over four octaves and attaches mouse listeners. */
    PianoPanel(MidiSynth synth) {
        this.synth = synth;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(42 * kw, kh + 1));
        int transpose = 24;
        int[] whiteIDs = {0, 2, 4, 5, 7, 9, 11};

        for (int i = 0, x = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++, x += kw) {
                int keyNum = i * 12 + whiteIDs[j] + transpose;
                synth.whiteKeys.add(new MidiKey(synth, x, 0, kw, kh, keyNum));
            }
        }
        for (int i = 0, x = 0; i < 6; i++, x += kw) {
            int keyNum = i * 12 + transpose;
            blackKeys.add(new MidiKey(synth, (x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 1));
            blackKeys.add(new MidiKey(synth, (x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 3));
            x += kw;
            blackKeys.add(new MidiKey(synth, (x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 6));
            blackKeys.add(new MidiKey(synth, (x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 8));
            blackKeys.add(new MidiKey(synth, (x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 10));
        }
        synth.keys.addAll(blackKeys);
        synth.keys.addAll(synth.whiteKeys);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (synth.mouseOverCB.isSelected()) {
                    MidiKey key = getKey(e.getPoint());
                    if (prevKey != null && prevKey != key) {
                        prevKey.off();
                    }
                    if (key != null && prevKey != key) {
                        key.on();
                    }
                    prevKey = key;
                    repaint();
                }
            }
        });
        addMouseListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        prevKey = getKey(e.getPoint());
        if (prevKey != null) {
            prevKey.on();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (prevKey != null) {
            prevKey.off();
            repaint();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (prevKey != null) {
            prevKey.off();
            repaint();
            prevKey = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * @param point mouse position in this component
     * @return the topmost key containing the point, or {@code null}
     */
    MidiKey getKey(Point point) {
        for (MidiKey key : synth.keys) {
            if (key.contains(point)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension d = getSize();

        g2.setBackground(getBackground());
        g2.clearRect(0, 0, d.width, d.height);

        g2.setColor(Color.white);
        g2.fillRect(0, 0, 42 * kw, kh);

        for (MidiKey key : synth.whiteKeys) {
            if (key.isNoteOn()) {
                g2.setColor(synth.record ? synth.pink : synth.jfcBlue);
                g2.fill(key);
            }
            g2.setColor(Color.black);
            g2.draw(key);
        }
        for (MidiKey key : blackKeys) {
            if (key.isNoteOn()) {
                g2.setColor(synth.record ? synth.pink : synth.jfcBlue);
                g2.fill(key);
                g2.setColor(Color.black);
                g2.draw(key);
            } else {
                g2.setColor(Color.black);
                g2.fill(key);
            }
        }
    }
}
