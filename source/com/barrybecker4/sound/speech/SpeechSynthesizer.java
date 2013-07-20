/*  Java Speech Synthesizer
 *  (C) LOTONtech Limited 2001
 */
package com.barrybecker4.sound.speech;

/**
 * Makes the somputer speak using speech synthesis.
 * See http://www.javaworld.com/javaworld/jw-08-2001/jw-0817-javatalk.html?page=1
 *
 * The speech engine works by concatenating short sound samples that represent the smallest
 * units of human -- in this case English -- speech.
 * Those sound samples, called allophones, are labeled with a one-, two-, or three-letter identifier.
 * Some identifiers are obvious and some not so obvious, as you can see from the phonetic
 * representation of the word "hello."
 *
 * Here is an example string you could pass in other to say "Please enter a number."
 *   "p|l|ee|z e|n|t|er aa nn|u|m|b|er .|.";
 *
    h -- sounds as you would expect
    e -- sounds as you would expect
    l -- sounds as you would expect, but notice that I've reduced a double "l" to a single one
    oo -- is the sound for "hello," not for "bot," and not for "too"

    Here is a list of the available allophones that can be used to compose words:

    a -- as in cat
    b -- as in cab
    c -- as in cat
    d -- as in dot
    e -- as in bet
    f -- as in frog
    g -- as in frog
    h -- as in hog
    i -- as in pig
    j -- as in jig
    k -- as in keg
    l -- as in leg
    m -- as in met
    n -- as in begin
    o -- as in not
    p -- as in pot
    r -- as in rot
    s -- as in sat
    t -- as in sat
    u -- as in put
    v -- as in have
    w -- as in wet
    y -- as in yet
    z -- as in zoo

    aa -- as in fake
    ay -- as in hay
    ee -- as in bee
    ii -- as in high
    oo -- as in go

    bb -- variation of b with different emphasis
    dd -- variation of d with different emphasis
    ggg -- variation of g with different emphasis
    hh -- variation of h with different emphasis
    ll -- variation of l with different emphasis
    nn -- variation of n with different emphasis
    rr -- variation of r with different emphasis
    tt -- variation of t with different emphasis
    yy -- variation of y with different emphasis

    ar -- as in car
    aer -- as in care
    ch -- as in which
    ck -- as in check
    ear -- as in beer
    er -- as in later
    err -- as in later (longer sound)
    ng -- as in feeding
    or -- as in law
    ou -- as in zoo
    ouu -- as in zoo (longer sound)
    ow -- as in cow
    oy -- as in boy
    sh -- as in shut
    th -- as in thing
    dth -- as in this
    uh -- variation of u
    wh -- as in where
    zh -- as in Asian
 */
public class SpeechSynthesizer {

    /**
     * This method speaks the given phonetic words.
     */
    public void sayText( String text )  {
        sayPhoneWords( text.split(" "));
    }

    /**
     * This method speaks the given phonetic words.
     */
    public void sayPhoneWords( String[] words ) {
        for (final String word : words) {
            new PhoneWord(word).say();
        }
    }

    /**
     * This method speaks a phonetic word specified on the command line.
     */
    public static void main( String args[] ) {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        if ( args.length > 0 ) {
            synthesizer.sayText(args[0]);
        }
        else {
            synthesizer.sayText("y|ouu c|a|nn a|d aa s|i|n|ggg|ll w|o|r|dd p|ar|a|m|e|tt|er .|.");
        }
        System.exit( 0 );
    }
}
