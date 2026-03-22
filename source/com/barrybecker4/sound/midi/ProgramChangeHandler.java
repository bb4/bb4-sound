package com.barrybecker4.sound.midi;


/**
 * Callbacks when the user picks a different MIDI program or table cell in {@link MidiSynth}.
 */
public interface ProgramChangeHandler {

    /**
     * Applies a new MIDI program to the current channel.
     *
     * @param program MIDI program number (0-based)
     */
    void programChange(int program);

    /**
     * Updates the instrument selection when the table row changes.
     *
     * @param row instrument table row
     */
    void newRow(int row);

    /**
     * Updates the instrument selection when the table column changes.
     *
     * @param newColumn instrument table column
     */
    void newColumn(int newColumn);
}
