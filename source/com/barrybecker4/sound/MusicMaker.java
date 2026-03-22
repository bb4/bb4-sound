package com.barrybecker4.sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 *  This class provides a facade to the Java Sound API.
 *  You can use it to generate all kinds of musical effects.
 *
 *  @author Barry Becker
 */
public class MusicMaker {

    /** Center value for {@link javax.sound.midi.MidiChannel#setPitchBend(int)} (14-bit, 0–16383). */
    private static final int PITCH_BEND_CENTER = 8192;

    private Synthesizer synthesizer;
    private Instruments instruments;
    private MidiChannel[] midiChannels;
    private MidiChannel channel;    // current channel


    /** Constructor  */
    public MusicMaker()  {
        initSynthesizer();
    }

    private void initSynthesizer() {
        if (!openSynthesizerIfAvailable()) {
            return;
        }

        Soundbank sb = synthesizer.getDefaultSoundbank();
        if (sb == null) {
            System.err.println("Error: no sound bank present on this system");
            closeSynthesizerQuietly();
            return;
        }

        instruments = new Instruments(sb);
        synthesizer.loadInstrument(instruments.getInstrument(0));

        midiChannels = synthesizer.getChannels();
        channel = midiChannels[0];
        configureChannel(channel);
    }

    private boolean openSynthesizerIfAvailable() {
        try {
            if (synthesizer == null) {
                synthesizer = MidiSystem.getSynthesizer();
                if (synthesizer == null) {
                    System.err.println("getSynthesizer() failed!");
                    return false;
                }
            }
            synthesizer.open();
            return true;
        } catch (MidiUnavailableException ex) {
            System.err.println("Midi Unavailable. No Synthesizer will be present.");
            ex.printStackTrace();
            return false;
        }
    }

    private void closeSynthesizerQuietly() {
        if (synthesizer != null) {
            synthesizer.close();
            synthesizer = null;
        }
    }

    private void configureChannel(MidiChannel ch) {
        ch.resetAllControllers();
        ch.setChannelPressure(64);
        ch.setPitchBend(PITCH_BEND_CENTER);
        ch.controlChange(91, 127); // reverb
        ch.setMute(false);
    }

    /**
     * Returns instruments from the default soundbank when initialization succeeded.
     *
     * @return instruments from the default soundbank, or {@code null} if none could be loaded
     *         (e.g. headless environment without a MIDI soundbank)
     */
    public Instruments getInstruments() {
        return instruments;
    }

    private boolean isReady() {
        return instruments != null && channel != null;
    }

    /**
     * Plays a note until stopped (sustained).
     *
     * @param instrumentType bank row group
     * @param instrumentSubType bank column within the row
     * @param note MIDI note number (0–127)
     * @param channelIndex MIDI channel (0–15)
     * @param velocity MIDI velocity (0–127)
     */
    public void startNote( int instrumentType, int instrumentSubType, int note,
                           int channelIndex, int velocity ) {
        if (!isReady()) return;
        initChannel( instrumentType, instrumentSubType, channelIndex );
        startNote( note, velocity );
    }

    /**
     * Starts a sustained note using the named instrument program.
     *
     * @param instrument exact instrument name from the soundbank
     * @param note MIDI note number (0–127)
     * @param channelIndex MIDI channel (0–15)
     * @param velocity MIDI velocity (0–127)
     */
    public void startNote( String instrument, int note,
                           int channelIndex, int velocity ) {
        if (!isReady()) return;
        int i = instruments.getInstrumentIndex( instrument );
        if ( i >= 0 )  {
            startNote( i/8, i % 8, note, channelIndex, velocity );
        }
    }

    /**
     * Plays a single note on one channel for a specified duration.
     *
     * @param instrumentType bank row group
     * @param instrumentSubType bank column within the row
     * @param note MIDI note number (0–127)
     * @param channelIndex MIDI channel (0–15)
     * @param duration hold time in milliseconds
     * @param velocity MIDI velocity (0–127)
     */
    public void playNote( int instrumentType, int instrumentSubType, int note,
                          int channelIndex, int duration, int velocity ) {
        assert channelIndex < 16 : "channel index must be 0-15";
        if (!isReady()) return;
        initChannel(instrumentType, instrumentSubType, channelIndex );
        playNote( note, duration, velocity );
    }

    /**
     * start/stop a note
     * @param note  MIDI note number (0–127)
     * @param velocity MIDI velocity (0–127)
     */
    public void startNote( int note, int velocity ) {
        if (!isReady()) return;
        channel.noteOn( note, velocity );
    }

    /**
     * Sends note-off for a playing note.
     *
     * @param note MIDI note number to release
     * @param velocity note-off velocity
     */
    public void stopNote( int note, int velocity ) {
        if (!isReady()) return;
        channel.noteOff( note, velocity );
    }

    /**
     * Plays a note for a fixed duration then sends note-off.
     *
     * @param note MIDI note number (0–127)
     * @param duration hold time in milliseconds
     * @param velocity MIDI velocity (0–127)
     */
    public void playNote( int note, int duration, int velocity ) {
        assert (note>=0);
        if (note > 127) {
            System.out.println( "note needs to be in the range 0-127 it is "+note );
            note = 127;
        }
        try {
            if (!isReady()) return;
            channel.noteOn( note, velocity );
            Thread.sleep( duration );
            channel.noteOff( note, velocity );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Play the specified csound/note
     * @param instrument instrument to use
     * @param note   note/frequency
     * @param channelIndex voice - there may be many
     * @param duration  duration in milliseconds
     * @param velocity MIDI velocity (0–127)
     */
    public void playNote( String instrument, int note,
                          int channelIndex, int duration, int velocity ) {
        if (!isReady()) return;
        int i = instruments.getInstrumentIndex( instrument );
        if ( i >= 0 ) {
            playNote( i / 8, i % 8, note, channelIndex, duration, velocity );
        }
    }

    /**
     * Loads the program and selects the MIDI channel for subsequent note methods.
     *
     * @param instrumentType bank row group
     * @param instrumentSubType bank column within the row
     * @param channelIndex MIDI channel (0–15)
     */
    protected void initChannel( int instrumentType, int instrumentSubType,
                                int channelIndex ) {
        if (!isReady()) return;
        int instrument = 8 * instrumentType + instrumentSubType;

        synthesizer.loadInstrument( instruments.getInstrument(instrument));
        channel = midiChannels[channelIndex];
        channel.programChange( instrument );
    }


    /** stop playing all music immediately */
    public void stopAllSounds() {
        if ( !isReady() ) return;
        channel.allNotesOff();
    }
}
