package com.barrybecker4.sound.speech;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhoneTokenMergeTest {

    @Test
    void computeMergeCount_zeroUnlessBothBuffersReachThreshold() {
        byte[] shortPrev = new byte[100];
        byte[] longCur = new byte[PhoneToken.MAX_MERGE_COUNT];
        assertEquals(0, PhoneToken.computeMergeCount(shortPrev, longCur, PhoneToken.MAX_MERGE_COUNT));

        byte[] longPrev = new byte[PhoneToken.MAX_MERGE_COUNT];
        assertEquals(PhoneToken.MAX_MERGE_COUNT,
                PhoneToken.computeMergeCount(longPrev, longCur, PhoneToken.MAX_MERGE_COUNT));
    }

    @Test
    void mergeAllophonesInPlace_averagesOverlappingRegion() {
        byte[] prev = new byte[]{0, 0, 10, 20};
        byte[] cur = new byte[]{30, 40, 0, 0};
        PhoneToken.mergeAllophonesInPlace(prev, cur, 2);
        assertEquals(20, prev[2]); // (10 + 30) / 2
        assertEquals(30, prev[3]); // (20 + 40) / 2
    }

    @Test
    void truncateAfterMerge_dropsMergedPrefixFromCurrent() {
        byte[] cur = new byte[]{1, 2, 3, 4, 5};
        byte[] tail = PhoneToken.truncateAfterMerge(cur, 2);
        assertArrayEquals(new byte[]{3, 4, 5}, tail);
    }

    @Test
    void truncateAfterMerge_zeroMergeCopiesFullBuffer() {
        byte[] cur = new byte[]{9, 8, 7};
        byte[] tail = PhoneToken.truncateAfterMerge(cur, 0);
        assertArrayEquals(cur, tail);
    }
}
