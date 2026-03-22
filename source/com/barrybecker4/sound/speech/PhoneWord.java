/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single phonetic word.
 * See http://www.javaworld.com/javaworld/jw-08-2001/jw-0817-javatalk.html
 * Represents and "says" a phonetic word.
 */
class PhoneWord {

    private String phoneWord;

    /**
     * Speaks the given phonetic word.
     */
    PhoneWord(String word) {
        phoneWord = word;
    }

    /**
     * Split a word into allophone tokens (delimiter {@code |}).
     * Empty or null input yields an empty list.
     */
    static List<String> splitAllophones(String word) {
        if (word == null || word.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(Arrays.asList(word.split("\\|", -1)));
    }

    /**
     * Speaks the given phonetic word.
     */
    void say() {
        byte[] previousSound = null;

        PhoneToken token = null;
        for (String t : splitAllophones(phoneWord)) {
            token = new PhoneToken(t);
            previousSound = token.process(previousSound);
        }

        if (token != null)  {
            token.playSound( previousSound );
            token.drain();
        }
    }
}
