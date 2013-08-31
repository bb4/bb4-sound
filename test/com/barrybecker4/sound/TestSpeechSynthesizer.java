/** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
package com.barrybecker4.sound;

import com.barrybecker4.sound.speech.SpeechSynthesizer;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Barry Becker
 */
public class TestSpeechSynthesizer {

    private static final String ENTER_NUMBER = "p|l|ee|z e|n|t|er aa nn|u|m|b|er .|.";

    private static final String[] PLAY_GAME = {"w|u|d", "y|ouu", "l|ii|k", "t|ouu", "p|l|ay", "aa", "gg|AY|M"};

    /** instance under test. */
    private static final SpeechSynthesizer speech = new SpeechSynthesizer();


    @Before
    public void setUp() {}

    @Test
    public void testSayEnterNumber() {
        speech.sayText( ENTER_NUMBER );
    }

    @Test
    public void testSayGreeting() {
        //speech.sayText("w|u|d y|ouu l|ii|k t|ouu p|l|ay aa gg|AY|M .");
        speech.sayPhoneWords( PLAY_GAME );
    }

    /** Why can't it say a as in apple?
    @Test
    public void testSayApple() {
        String text = "a|p|l  1sp AA_|P_|l 10ms AA_|p|L_ 50ms AA_|p|LL";
        speech.sayText(text);
    } */

}

