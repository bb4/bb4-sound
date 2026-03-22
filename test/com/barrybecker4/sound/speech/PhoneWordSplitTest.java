package com.barrybecker4.sound.speech;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneWordSplitTest {

    @Test
    void splitAllophones_nullOrEmpty() {
        assertTrue(PhoneWord.splitAllophones(null).isEmpty());
        assertTrue(PhoneWord.splitAllophones("").isEmpty());
    }

    @Test
    void splitAllophones_simpleTokens() {
        assertEquals(List.of("h", "e", "ll", "oo"), PhoneWord.splitAllophones("h|e|ll|oo"));
    }

    @Test
    void splitAllophones_preservesTrailingEmptySegment() {
        assertEquals(List.of("a", ""), PhoneWord.splitAllophones("a|"));
    }
}
