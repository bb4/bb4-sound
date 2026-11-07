// Copyright by Barry G. Becker, 2012. Licensed under MIT License: http://www.opensource.org/licenses/MIT
package com.barrybecker4.sound

import javax.sound.midi.{Instrument, Soundbank}

/**
 * A collection of instruments and other sounds that can be made audible using the Java Sound API.
 * You can use these instruments to generate all kinds of musical effects.
 *
 * @author Barry Becker
 */
object Instruments {
  val PIANO: String = "Piano"
  val ORGAN: String = "Reed Organ"
  val VIOLIN: String = "Violin"
  val ORCHESTRA_HIT: String = "Orchestra Hit"
  val TRUMPET: String = "Trumpet"
  val TROMBONE: String = "Trombone"
  val TUBA: String = "Tuba"
  val WHISTLE: String = "Whistle"
  val WARM_PAD: String = "Warm Pad"
  val GOBLINS: String = "Goblins"
  val DROPS: String = "Echo Drops"
  val SITAR: String = "Sitar"
  val SHAMISEN: String = "Shamisen"
  val STEEL_DRUMS: String = "Steel Drums"
  val WOODBLOCK: String = "Woodblock"
  val TAIKO_DRUM: String = "Taiko Drum"
  val GUITAR_FRET: String = "Guitar Fret Noise"
  val SEASHORE: String = "Seashore"
  val BIRD: String = "Bird"
  val TELEPHONE: String = "Telephone"
  val HELICOPTER: String = "Helicopter"
  val APPLAUSE: String = "Applause"
  val GUNSHOT: String = "Gunshot"
  val WATERY_GLASS: String = "Watery Glass"
  val DROPLET2: String = "Droplet 2"
  val AIRPLANE: String = "Digi-Dodo"
  val ALARM: String = "Alarm"
  val BUZZY_HIT: String = "buzzy hit"
  val GATE_TONE: String = "Gate-tone"
  val DUB_KICK: String = "dub_kick"
  val FIFTH_PULSE: String = "5th Pulse"
  val RICOCHET: String = "Ricochet Pad"
  val ANAL_SEQ: String = "Analog Sequence"
  val GOBLINS2: String = "Goblins 2"
  val VID_GAME2: String = "Video Game 2"
  val HOVERBUG: String = "Hoverbug"
  val WHIPPED: String = "Whipped"
  val POING: String = "Poing"
  val METAL_SRYAY: String = "Metal Spray"
  val FLYBY2: String = "FlyBy 2"
  val COSMIC_RAY: String = "Cosmic Ray"
  val SCIENCE_TOM: String = "ScienceTom"
  val SLO_LASER: String = "SloLaser"
  val SCRATCH: String = "itchy-scratch"
  val SCRATCH2: String = "itchy-scratch2"
  val METALLIC_SNARE: String = "hi_metallic_snare"
  val CHEM_TONE: String = "chem-tone"

  /** These sound the best in my opinion. */
  val FAVORITES: Array[String] = Array(
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
  )
}

class Instruments private[sound] (soundBank: Soundbank) {

  private val instruments: Array[Instrument] = soundBank.getInstruments

  def getInstrument(i: Int): Instrument = instruments(i)

  def getNumInstruments: Int = instruments.length

  /** @param instrumentType row group (0-based) */
  private[sound] def getInstrumentName(instrumentType: Int, instrumentSubType: Int): String = {
    val i = instrumentSubType + 8 * instrumentType
    instruments(i).getName
  }

  /** @param instrument exact name as returned by [[Instrument.getName]] */
  private[sound] def getInstrumentIndex(instrument: String): Int = {
    var i = 0
    while (i < instruments.length) {
      if (instruments(i).getName == instrument) return i
      i += 1
    }
    System.out.println("not found ****:     " + instrument)
    -1
  }
}
