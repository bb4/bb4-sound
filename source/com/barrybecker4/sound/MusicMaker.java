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

    private Synthesizer synthesizer;
    private Instruments instruments;
    private MidiChannel[] midiChannels;
    private MidiChannel channel;    // current channel


    /** Constructor  */
    public MusicMaker()  {
        initSynthesizer();
    }

    private void initSynthesizer() {
        try {
            if (synthesizer == null) {
                if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
                    System.out.println("getSynthesizer() failed!");
                    return;
                }
            }
            synthesizer.open();
        }
        catch (MidiUnavailableException ex) {
            System.out.println("Midi Anavailable. No Synthesizer will be present.");
            ex.printStackTrace();
            return;
        }

        Soundbank sb = synthesizer.getDefaultSoundbank();

        if ( sb != null ) {
            instruments = new Instruments(synthesizer.getDefaultSoundbank());
            synthesizer.loadInstrument( instruments.getInstrument(0) );
        }
        else {
            System.out.println( "Error: no sound bank present on this system" );
            assert false : "no sound bank";
        }

        midiChannels = synthesizer.getChannels();
        //System.out.println("num midi channels = "+midiChannels_.magnitude);
        channel = midiChannels[0];

        channel.resetAllControllers();
        channel.setChannelPressure( 128 );
        channel.setPitchBend( 128 );
        channel.controlChange( 91, 128 ); // reverb
        channel.setMute( false );
        //channel_.allNotesOff();
    }

    public Instruments getInstruments() {
        return instruments;
    }


    /**
     * will play a note until stopped
     */
    public void startNote( int instrumentType, int instrumentSubType, int note,
                           int channelIndex, int velocity ) {
        initChannel( instrumentType, instrumentSubType, channelIndex );
        startNote( note, velocity );
    }

    public void startNote( String instrument, int note,
                           int channelIndex, int velocity ) {
        int i = instruments.getInstrumentIndex( instrument );
        if ( i >= 0 )  {
            startNote( i/8, i % 8, note, channelIndex, velocity );
        }
    }

    /**
     * Lets you play a single note on one channel for a specified duration.
     */
    public void playNote( int instrumentType, int instrumentSubType, int note,
                          int channelIndex, int duration, int velocity ) {
        assert channelIndex < 16 : "channel index must be 0-15";
        initChannel(instrumentType, instrumentSubType, channelIndex );
        playNote( note, duration, velocity );
    }

    /**
     * start/stop a note
     * @param note  the pitch (1-100) (20-70 reasonable)
     * @param velocity loudness/volume (0 - 1000) (Can't hear 100)
     */
    public void startNote( int note, int velocity ) {
        channel.noteOn( note, velocity );
    }

    public void stopNote( int note, int velocity ) {
        channel.noteOff( note, velocity );
    }

    /**
     * @param note  the pitch (1-100) (20-70 reasonable)
     * @param duration in milliseconds
     * @param velocity loudness/volume (0 - 1000) (Can't hear 100 for some sounds)
     */
    public void playNote( int note, int duration, int velocity ) {
        assert (note>=0);
        if (note > 127) {
            System.out.println( "note needs to be in the range 0-127 it is "+note );
            note = 127;
        }
        try {
            if (channel == null) return;
            //System.out.println(note+"  "+duration);
            channel.noteOn( note, velocity );
            Thread.sleep( duration );
            channel.noteOff( note, velocity );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Play the specified csound/note
     * @param instrument instrument to use
     * @param note   note/frequency
     * @param channelIndex voice - there may be many
     * @param duration  duration in milliseconds
     * @param velocity loudness/volume (0 - 1000) (Can't hear 100 for some sounds)
     */
    public void playNote( String instrument, int note,
                          int channelIndex, int duration, int velocity ) {
        int i = instruments.getInstrumentIndex( instrument );
        if ( i >= 0 ) {
            playNote( i / 8, i % 8, note, channelIndex, duration, velocity );
        }
    }

    protected void initChannel( int instrumentType, int instrumentSubType,
                                int channelIndex ) {
        int instrument = 8 * instrumentType + instrumentSubType;

        synthesizer.loadInstrument( instruments.getInstrument(instrument));
        channel = midiChannels[channelIndex];
        channel.programChange( instrument );
    }


    /** stop playing all music immediately */
    public void stopAllSounds() {
        if ( channel == null ) return;
        channel.allNotesOff();
    }
}
