package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

class PhoneWordSplitSuite extends AnyFunSuite {

  test("splitAllophones null or empty") {
    assert(PhoneWord.splitAllophones(null).isEmpty)
    assert(PhoneWord.splitAllophones("").isEmpty)
  }

  test("splitAllophones simple tokens") {
    assertResult(List("h", "e", "ll", "oo")) {
      PhoneWord.splitAllophones("h|e|ll|oo")
    }
  }

  test("splitAllophones preserves trailing empty segment") {
    assertResult(List("a", "")) {
      PhoneWord.splitAllophones("a|")
    }
  }
}
