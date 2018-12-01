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

    private Instrument instruments_[];

    // list all the instruments here so they are easy to choose from
    // This is just my favorite subset of those that are available
    public static final String PIANO = "Piano";
    public static final String ORGAN = "Reed Organ";
    public static final String VIOLIN = "Violin";
    public static final String ORCHESTRA_HIT = "Orchestra Hit";
    public static final String TRUMPET = "Trumpet";
    public static final String TROMBONE = "Trombone";
    public static final String TUBA = "Tuba";
    public static final String WHISTLE = "Whistle";
    public static final String WARM_PAD = "Warm Pad";
    public static final String GOBLINS = "Goblins";
    public static final String DROPS = "Echo Drops";
    public static final String SITAR = "Sitar";
    public static final String SHAMISEN = "Shamisen";
    public static final String STEEL_DRUMS = "Steel Drums";
    public static final String WOODBLOCK = "Woodblock";
    public static final String TAIKO_DRUM = "Taiko Drum";
    public static final String GUITAR_FRET = "Guitar Fret Noise";
    public static final String SEASHORE = "Seashore";
    public static final String BIRD = "Bird";
    public static final String TELEPHONE = "Telephone";
    public static final String HELICOPTER = "Helicopter";
    public static final String APPLAUSE = "Applause";
    public static final String GUNSHOT = "Gunshot";
    public static final String WATERY_GLASS = "Watery Glass";
    public static final String DROPLET2 = "Droplet 2";
    public static final String AIRPLANE = "Digi-Dodo";
    public static final String ALARM = "Alarm";
    public static final String BUZZY_HIT = "buzzy hit";
    public static final String GATE_TONE = "Gate-tone";      // futuristic door
    public static final String DUB_KICK = "dub_kick";        // spacy
    public static final String FIFTH_PULSE = "5th Pulse";
    public static final String RICOCHET = "Ricochet Pad";
    public static final String ANAL_SEQ = "Analog Sequence";
    public static final String GOBLINS2 = "Goblins 2";
    public static final String VID_GAME2 = "Video Game 2";
    public static final String HOVERBUG = "Hoverbug";
    public static final String WHIPPED = "Whipped";
    public static final String POING = "Poing";
    public static final String METAL_SRYAY = "Metal Spray";
    public static final String FLYBY2 = "FlyBy 2";
    public static final String COSMIC_RAY = "Cosmic Ray";
    public static final String SCIENCE_TOM = "ScienceTom";
    public static final String SLO_LASER = "SloLaser";
    public static final String SCRATCH = "itchy-scratch";
    public static final String SCRATCH2 = "itchy-scratch2";
    public static final String METALLIC_SNARE = "hi_metallic_snare";    // reverberation
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
        instruments_ = soundBank.getInstruments();
    }

    public Instrument getInstrument(int i) {
        return instruments_[i];
    }

    public int getNumInstruments() {
        return instruments_.length;
    }

    String getInstrumentName( int instrumentType, int instrumentSubType ) {
        int i = instrumentSubType + 8 * instrumentType;
        return instruments_[i].getName();
    }

    /** @return the index for the specified instrument */
    int getInstrumentIndex( String instrument ) {
        int i = 0;
        if ( instruments_ == null ) return -1;
        while ( i < instruments_.length && !(instruments_[i].getName().equals( instrument )) )  {
            i++;
        }
        if ( i == instruments_.length ) {
            System.out.println( "not found ****:     " + instrument );
            return -1;
        }
        else {
            return i;
        }
    }
}
