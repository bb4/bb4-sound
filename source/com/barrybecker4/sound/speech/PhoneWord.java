/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech;

import java.util.StringTokenizer;

/**
 * Represents a single phonetic word.
 * See http://www.javaworld.com/javaworld/jw-08-2001/jw-0817-javatalk.html?page=1
 * Represents and "says" a phonetic word.
 */
class PhoneWord {

    private String phoneWord;

    /**
     * This method speaks the given phonetic word.
     */
    PhoneWord(String word) {
        phoneWord = word;
    }

    /**
     * This method speaks the given phonetic word.
     */
    void say() {
        // -- set up a dummy byte array for the previous sound --
        byte[] previousSound = null;

        // -- split the input string into separate allophones --
        StringTokenizer st = new StringTokenizer( phoneWord, "|", false );

        //System.out.println("about to say: "+ word);
        PhoneToken token = null;
        while ( st.hasMoreTokens() ) {
            token = new PhoneToken(st.nextToken());
            previousSound = token.process(previousSound);
        }

        // -- play the final sound and drain the sound channel --
        if (token != null)  {
            token.playSound( previousSound );
            token.drain();
        }
    }
}
