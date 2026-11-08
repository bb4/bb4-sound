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

  test("merge averages big-endian 16-bit samples") {
    val prev = Array[Byte](0x01, 0x00)          // sample 256
    val cur  = Array[Byte](0x00, 0xff.toByte)   // sample 255
    PhoneToken.mergeAllophonesInPlace(prev, cur, 2)
    assertResult(Array[Byte](0x00, 0xff.toByte))(prev) // 255, not 0
  }

  test("merge averages negative big-endian 16-bit samples") {
    val prev = Array[Byte](0x7f, 0x00, 0xff.toByte, 0xfe.toByte) // 32512, then -2
    val cur = Array[Byte](0x00, 0x00)                            // 0
    PhoneToken.mergeAllophonesInPlace(prev, cur, 2)
    assertResult(Array[Byte](0x7f, 0x00, 0xff.toByte, 0xff.toByte))(prev) // prefix kept, average -1
  }

  test("merge averages every big-endian frame in the overlap") {
    val prev = Array[Byte](0x01, 0x00, 0xff.toByte, 0xfe.toByte) // 256, -2
    val cur = Array[Byte](0x00, 0xff.toByte, 0x00, 0x00)         // 255, 0
    PhoneToken.mergeAllophonesInPlace(prev, cur, 4)
    assertResult(Array[Byte](0x00, 0xff.toByte, 0xff.toByte, 0xff.toByte))(prev) // 255, -1
  }

  test("truncateAfterMerge drops merged prefix from current") {
    val cur = Array[Byte](1, 2, 3, 4, 5)
    assertResult(Array[Byte](3, 4, 5))(PhoneToken.truncateAfterMerge(cur, 2))
  }

  test("truncateAfterMerge keeps a trailing partial frame") {
    val cur = Array[Byte](1, 2, 3, 4, 5)
    assertResult(Array[Byte](3, 4, 5))(PhoneToken.truncateAfterMerge(cur, 3))
  }

  test("merge leaves bytes outside a whole frame untouched") {
    val prev = Array[Byte](0x01, 0x00, 0x00, 0x04) // 256, then 4
    val cur = Array[Byte](0x00, 0x02, 0x00, 0x06)  // 2, then 6
    PhoneToken.mergeAllophonesInPlace(prev, cur, 3)
    assertResult(Array[Byte](0x01, 0x00, 0x00, 0x03))(prev) // first sample kept; 4 and 2 average to 3
  }

  test("truncateAfterMerge zero merge copies full buffer") {
    val cur = Array[Byte](9, 8, 7)
    assertResult(cur)(PhoneToken.truncateAfterMerge(cur, 0))
  }
}
