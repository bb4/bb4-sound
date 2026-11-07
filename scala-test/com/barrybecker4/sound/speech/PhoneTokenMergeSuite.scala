package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

class PhoneTokenMergeSuite extends AnyFunSuite {

  test("computeMergeCount zero unless both buffers reach threshold") {
    val shortPrev = new Array[Byte](100)
    val longCur = new Array[Byte](PhoneToken.MAX_MERGE_COUNT)
    assertResult(0)(PhoneToken.computeMergeCount(shortPrev, longCur, PhoneToken.MAX_MERGE_COUNT))

    val longPrev = new Array[Byte](PhoneToken.MAX_MERGE_COUNT)
    assertResult(PhoneToken.MAX_MERGE_COUNT)(
      PhoneToken.computeMergeCount(longPrev, longCur, PhoneToken.MAX_MERGE_COUNT)
    )
  }

  test("mergeAllophonesInPlace averages overlapping region") {
    val prev = Array[Byte](0, 0, 10, 20)
    val cur = Array[Byte](30, 40, 0, 0)
    PhoneToken.mergeAllophonesInPlace(prev, cur, 2)
    assertResult(20)(prev(2))
    assertResult(30)(prev(3))
  }

  test("truncateAfterMerge drops merged prefix from current") {
    val cur = Array[Byte](1, 2, 3, 4, 5)
    assertResult(Array[Byte](3, 4, 5))(PhoneToken.truncateAfterMerge(cur, 2))
  }

  test("truncateAfterMerge zero merge copies full buffer") {
    val cur = Array[Byte](9, 8, 7)
    assertResult(cur)(PhoneToken.truncateAfterMerge(cur, 0))
  }
}
