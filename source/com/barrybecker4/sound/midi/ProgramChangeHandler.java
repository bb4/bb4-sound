package com.barrybecker4.sound.midi;


public interface ProgramChangeHandler {

    void programChange(int program);

    void newRow(int row);

    void newColumn(int newColumn);
}
