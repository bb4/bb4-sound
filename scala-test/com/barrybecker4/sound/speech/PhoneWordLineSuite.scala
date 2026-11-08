package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

import java.io.OutputStream
import java.io.PrintStream

import scala.collection.mutable.ListBuffer

class PhoneWordLineSuite extends AnyFunSuite {

  // Allophone filenames are case-sensitive (aa.au, oo.au, p.au).
  test("saying a word closes the single line") {
    val lines = ListBuffer.empty[FakeLine]
    val word = trackingWord("aa|oo|p", lines)
    word.say()
    assertResult(1)(lines.size)
    assert(lines.forall(_.closed))
    assertResult(1)(lines.head.closeCount)
    assert(lines.head.drainedBeforeClose)
    assert(lines.head.bytesWritten > 0)
    assertResult(3)(lines.head.writeCount)
  }

  test("saying an empty word opens no line") {
    val lines = ListBuffer.empty[FakeLine]
    trackingWord("", lines).say()
    assertResult(0)(lines.size)
  }

  test("punctuation opens no line") {
    val lines = ListBuffer.empty[FakeLine]
    trackingWord(",", lines).say()
    assertResult(0)(lines.size)
  }

  test("trailing punctuation still closes the single line") {
    val lines = ListBuffer.empty[FakeLine]
    trackingWord("aa|oo|.", lines).say()
    assertResult(1)(lines.size)
    assert(lines.forall(_.closed))
    assert(lines.head.drainedBeforeClose)
  }

  test("a missing allophone still closes the opened line") {
    val lines = ListBuffer.empty[FakeLine]
    val word = trackingWord("aa|no-such-allophone", lines)
    withoutStdErr {
      assertThrows[AssertionError] {
        word.say()
      }
    }
    assertResult(1)(lines.size)
    assert(lines.forall(_.closed))
    assertResult(1)(lines.head.closeCount)
  }

  private def withoutStdErr(body: => Unit): Unit = {
    val previous = System.err
    System.setErr(new PrintStream(OutputStream.nullOutputStream()))
    try body
    finally System.setErr(previous)
  }

  private def trackingWord(phones: String, lines: ListBuffer[FakeLine]): PhoneWord =
    new PhoneWord(phones, lineFactory = { _ =>
      val line = new FakeLine
      lines += line
      line
    })
}

/** Counts opens (constructions) and closes without touching AudioSystem. */
private final class FakeLine extends SampleLine {
  private val events = ListBuffer.empty[String]
  var bytesWritten: Int = 0

  def closed: Boolean = events.contains("close")
  def closeCount: Int = events.count(_ == "close")
  def writeCount: Int = events.count(_ == "write")

  def drainedBeforeClose: Boolean = {
    val drainAt = events.indexOf("drain")
    val closeAt = events.indexOf("close")
    drainAt >= 0 && closeAt > drainAt
  }

  override def write(data: Array[Byte], offset: Int, length: Int): Unit = {
    events += "write"
    bytesWritten += length
  }

  override def drain(): Unit = events += "drain"
  override def flush(): Unit = events += "flush"
  override def close(): Unit = events += "close"
  override def getBufferSize: Int = 50000
}
