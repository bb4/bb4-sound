package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

import scala.jdk.CollectionConverters.*

class PhoneWordSplitSuite extends AnyFunSuite {

  test("splitAllophones null or empty") {
    assert(PhoneWord.splitAllophones(null).isEmpty)
    assert(PhoneWord.splitAllophones("").isEmpty)
  }

  test("splitAllophones simple tokens") {
    assertResult(List("h", "e", "ll", "oo")) {
      PhoneWord.splitAllophones("h|e|ll|oo").asScala.toList
    }
  }

  test("splitAllophones preserves trailing empty segment") {
    assertResult(List("a", "")) {
      PhoneWord.splitAllophones("a|").asScala.toList
    }
  }
}
