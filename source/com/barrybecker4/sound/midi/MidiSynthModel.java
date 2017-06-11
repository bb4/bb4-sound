package com.barrybecker4.sound.midi;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import java.io.File;
import java.io.IOException;


/**
 * MidiSynch Model code extracted from MidiSynth by Brian Lichtenwalter.
 *
 * @author Brian Lichtenwalter
 * @author Barry Becker
 */
public class MidiSynthModel  {

    private Sequencer sequencer;
    private Sequence sequence;
    private Synthesizer synthesizer;
    private Instrument instruments[];


    public MidiSynthModel() {}

    public MidiChannel[] getMidiChannels() {
        return synthesizer.getChannels();
    }

    public int getResolution() {
        return sequence.getResolution();
    }

    public boolean hasInstruments()  {
        return instruments != null;
    }
    public Instrument getInstrument(int i) {
        return instruments[i];
    }

    public void loadInstrument(int program) {
        synthesizer.loadInstrument(getInstrument(program));
    }

    public Sequencer getSequencer() {
        return sequencer;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void saveMidiFile(File file) {
        try {
            int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
            if (fileTypes.length == 0) {
                System.out.println("Can't save sequence");
            } else {
                if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
                    throw new IOException("Problems writing to file");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addSequence(MetaEventListener listener) {
        sequencer.addMetaEventListener(listener);
        try {
            sequence = new Sequence(Sequence.PPQ, 10);
        } catch (Exception ex) { ex.printStackTrace(); }
    }


    public void open() {
        try {
            if (synthesizer == null) {
                if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
                    System.out.println("getSynthesizer() failed!");
                    return;
                }
            }
            synthesizer.open();
            sequencer = MidiSystem.getSequencer();
            sequence = new Sequence(Sequence.PPQ, 10);
        } catch (Exception ex) { ex.printStackTrace(); return; }

        Soundbank sb = synthesizer.getDefaultSoundbank();
        if (sb != null) {
            instruments = synthesizer.getDefaultSoundbank().getInstruments();
            synthesizer.loadInstrument(instruments[0]);
        }
    }


    public void close() {
        if (synthesizer != null) {
            synthesizer.close();
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
        synthesizer = null;
        instruments = null;
    }

}
