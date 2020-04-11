/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.sound;

import com.barrybecker4.sound.speech.SpeechSynthesizer;
import org.junit.Test;

/**
 * @author Barry Becker
 */
public class TestSpeechSynthesizer {

    private static final String ENTER_NUMBER = "p|l|ee|z e|n|t|er aa nn|u|m|b|er .|.";

    private static final String[] PLAY_GAME = {"w|u|d", "y|ouu", "l|ii|k", "t|ouu", "p|l|ay", "aa", "gg|AY|M"};

    private static final String[] GIVE_UP = {
            "ii", "g|i|v", "u|p|.", "th|a|t", "n|u|m|b|e|r", "i|s", "t|oo", "b|i|g", "ee|v|e|n", "f|o|r", "m|ee|."
    };

    /** instance under test. */
    private static final SpeechSynthesizer speech = new SpeechSynthesizer();


    @Test
    public void testSayEnterNumber() {
        speech.sayText( ENTER_NUMBER );
    }

    @Test
    public void testSayGreeting() {
        speech.sayPhoneWords( PLAY_GAME );
    }

//    @Test
//    public void testGiveUp() {
//        speech.sayPhoneWords( GIVE_UP );
//    }

    /** Why can't it say 'a' as in apple?
    @Test
    public void testSayApple() {
        String text = "a|p|l 1sp 1sp OU|P_|l 10ms AA_|p|L_ 50ms AA_|p|LL";
        speech.sayText(text);
    }*/
}
