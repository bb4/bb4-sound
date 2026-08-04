// Copyright by Barry G. Becker, 2012. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound;

import javax.sound.midi.Instrument;
import javax.sound.midi.Soundbank;

/**
 *  A collection of instruments and other sounds that can be made audible using the Java Sound API.
 *  You can use these instruments to generate all kinds of musical effects.
 *
 *  @author Barry Becker
 */
public class Instruments {

    /** Instruments from the loaded soundbank (same order as the bank). */
    private final Instrument[] instruments;

    // list all the instruments here so they are easy to choose from
    // This is just my favorite subset of those that are available
    /** {@value} */
    public static final String PIANO = "Piano";
    /** {@value} */
    public static final String ORGAN = "Reed Organ";
    /** {@value} */
    public static final String VIOLIN = "Violin";
    /** {@value} */
    public static final String ORCHESTRA_HIT = "Orchestra Hit";
    /** {@value} */
    public static final String TRUMPET = "Trumpet";
    /** {@value} */
    public static final String TROMBONE = "Trombone";
    /** {@value} */
    public static final String TUBA = "Tuba";
    /** {@value} */
    public static final String WHISTLE = "Whistle";
    /** {@value} */
    public static final String WARM_PAD = "Warm Pad";
    /** {@value} */
    public static final String GOBLINS = "Goblins";
    /** {@value} */
    public static final String DROPS = "Echo Drops";
    /** {@value} */
    public static final String SITAR = "Sitar";
    /** {@value} */
    public static final String SHAMISEN = "Shamisen";
    /** {@value} */
    public static final String STEEL_DRUMS = "Steel Drums";
    /** {@value} */
    public static final String WOODBLOCK = "Woodblock";
    /** {@value} */
    public static final String TAIKO_DRUM = "Taiko Drum";
    /** {@value} */
    public static final String GUITAR_FRET = "Guitar Fret Noise";
    /** {@value} */
    public static final String SEASHORE = "Seashore";
    /** {@value} */
    public static final String BIRD = "Bird";
    /** {@value} */
    public static final String TELEPHONE = "Telephone";
    /** {@value} */
    public static final String HELICOPTER = "Helicopter";
    /** {@value} */
    public static final String APPLAUSE = "Applause";
    /** {@value} */
    public static final String GUNSHOT = "Gunshot";
    /** {@value} */
    public static final String WATERY_GLASS = "Watery Glass";
    /** {@value} */
    public static final String DROPLET2 = "Droplet 2";
    /** {@value} */
    public static final String AIRPLANE = "Digi-Dodo";
    /** {@value} */
    public static final String ALARM = "Alarm";
    /** {@value} */
    public static final String BUZZY_HIT = "buzzy hit";
    /** {@value} */
    public static final String GATE_TONE = "Gate-tone";
    /** {@value} */
    public static final String DUB_KICK = "dub_kick";
    /** {@value} */
    public static final String FIFTH_PULSE = "5th Pulse";
    /** {@value} */
    public static final String RICOCHET = "Ricochet Pad";
    /** {@value} */
    public static final String ANAL_SEQ = "Analog Sequence";
    /** {@value} */
    public static final String GOBLINS2 = "Goblins 2";
    /** {@value} */
    public static final String VID_GAME2 = "Video Game 2";
    /** {@value} */
    public static final String HOVERBUG = "Hoverbug";
    /** {@value} */
    public static final String WHIPPED = "Whipped";
    /** {@value} */
    public static final String POING = "Poing";
    /** {@value} */
    public static final String METAL_SRYAY = "Metal Spray";
    /** {@value} */
    public static final String FLYBY2 = "FlyBy 2";
    /** {@value} */
    public static final String COSMIC_RAY = "Cosmic Ray";
    /** {@value} */
    public static final String SCIENCE_TOM = "ScienceTom";
    /** {@value} */
    public static final String SLO_LASER = "SloLaser";
    /** {@value} */
    public static final String SCRATCH = "itchy-scratch";
    /** {@value} */
    public static final String SCRATCH2 = "itchy-scratch2";
    /** {@value} */
    public static final String METALLIC_SNARE = "hi_metallic_snare";
    /** {@value} */
    public static final String CHEM_TONE = "chem-tone";

    /** these sound the best in my opinion. */
    public static final String[] FAVORITES = {
            PIANO, ORGAN, VIOLIN,
            ORCHESTRA_HIT, TRUMPET, TROMBONE,
            TUBA, WHISTLE, WARM_PAD, GOBLINS,
            DROPS, SITAR, SHAMISEN, STEEL_DRUMS, WOODBLOCK,
            TAIKO_DRUM, GUITAR_FRET, SEASHORE, BIRD,
            TELEPHONE, HELICOPTER, APPLAUSE, GUNSHOT,
            WATERY_GLASS, DROPLET2, AIRPLANE, ALARM,
            BUZZY_HIT, GATE_TONE, DUB_KICK,
            FIFTH_PULSE, RICOCHET,
            ANAL_SEQ, GOBLINS2, VID_GAME2,
            HOVERBUG, WHIPPED, POING, METAL_SRYAY, FLYBY2,
            COSMIC_RAY, SCIENCE_TOM, SLO_LASER,
            SCRATCH, SCRATCH2, METALLIC_SNARE, CHEM_TONE
    };


    /** Constructor  */
    Instruments(Soundbank soundBank)  {
        this.instruments = soundBank.getInstruments();
    }

    /**
     * Returns the instrument at the given index.
     *
     * @param i index into {@link #instruments}
     * @return the MIDI instrument at that index
     */
    public Instrument getInstrument(int i) {
        return instruments[i];
    }

    /**
     * Returns how many instruments the loaded bank contains.
     *
     * @return number of instruments in the bank
     */
    public int getNumInstruments() {
        return instruments.length;
    }

    /**
     * @param instrumentType row group (0-based)
     * @param instrumentSubType column within the row
     * @return display name of the combined program
     */
    String getInstrumentName( int instrumentType, int instrumentSubType ) {
        int i = instrumentSubType + 8 * instrumentType;
        return instruments[i].getName();
    }

    /**
     * @param instrument exact name as returned by {@link Instrument#getName()}
     * @return bank index, or {@code -1} if not found
     */
    int getInstrumentIndex( String instrument ) {
        for (int i = 0; i < instruments.length; i++) {
            if (instruments[i].getName().equals(instrument)) {
                return i;
            }
        }
        System.out.println("not found ****:     " + instrument);
        return -1;
    }
}
