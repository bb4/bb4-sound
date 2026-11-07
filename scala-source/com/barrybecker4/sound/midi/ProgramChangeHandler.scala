package com.barrybecker4.sound.midi

/** Callbacks when the user picks a different MIDI program or table cell in [[MidiSynth]]. */
trait ProgramChangeHandler {

  /** Applies a new MIDI program to the current channel. */
  def programChange(program: Int): Unit

  /** Updates the instrument selection when the table row changes. */
  def newRow(row: Int): Unit

  /** Updates the instrument selection when the table column changes. */
  def newColumn(newColumn: Int): Unit
}
