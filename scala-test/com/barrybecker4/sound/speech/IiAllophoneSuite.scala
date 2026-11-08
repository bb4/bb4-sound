package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

/**
 * `ii` is the vowel in "high" (as in "like"). The file used to be a byte copy of the
 * `h_` recording, so classpath lookup already resolved `ii` to the wrong sound.
 * These checks read the resources only and do not open a mixer.
 */
class IiAllophoneSuite extends AnyFunSuite {

  private val loader = getClass.getClassLoader

  test("ii is not a copy of H_") {
    val ii = allophone("ii.au")
    // This branch stores that recording at h_.au.
    val h = allophone("h_.au")
    assert(!java.util.Arrays.equals(ii, h))
  }

  test("ii is a distinct mu-law high vowel") {
    val ii = allophone("ii.au")
    val h = allophone("h_.au")
    val iiAu = parseAu(ii)
    val hAu = parseAu(h)

    assert(iiAu.encoding == 1)
    assert(iiAu.sampleRate == 8012)
    assert(iiAu.channels == 1)
    assert(iiAu.pcm.length >= 1400 && iiAu.pcm.length <= 3200)

    val diffs = payloadDifferences(ii, iiAu, h, hAu)
    val span = math.max(iiAu.dataSize, hAu.dataSize)
    assert(diffs > span / 2, s"ii.au differs from h_.au in only $diffs of $span bytes")

    val rms = math.sqrt(iiAu.pcm.map(sample => sample * sample).sum / iiAu.pcm.length)
    assert(rms > 4000.0)

    // Opening of /aɪ/ is energy near 700 Hz; the offglide is energy near 2 kHz.
    val early = slice(iiAu.pcm, 0.12, 0.38)
    val late = slice(iiAu.pcm, 0.62, 0.88)
    val earlySpectrum = powerSpectrum(early)
    val lateSpectrum = powerSpectrum(late)
    val earlyLow = bandEnergy(earlySpectrum, early.length, iiAu.sampleRate, 500, 950)
    val earlyHigh = bandEnergy(earlySpectrum, early.length, iiAu.sampleRate, 1700, 2400)
    val lateLow = bandEnergy(lateSpectrum, late.length, iiAu.sampleRate, 500, 950)
    val lateHigh = bandEnergy(lateSpectrum, late.length, iiAu.sampleRate, 1700, 2400)
    assert(earlyHigh / earlyLow < 0.2)
    assert(lateHigh / lateLow > 20.0)
    assert(lateHigh / earlyHigh > 8.0)

    val duplicates = allophoneNames.filter { name =>
      name != "ii.au" && allophone(name).sameElements(ii)
    }
    assert(duplicates.isEmpty, duplicates.mkString("ii.au duplicates ", ", ", ""))
  }

  private def allophone(name: String): Array[Byte] = {
    val path = PhoneToken.RESOURCE_PATH + name
    val in = loader.getResourceAsStream(path)
    assert(in != null, path)
    try in.readAllBytes()
    finally in.close()
  }

  private def allophoneNames: Seq[String] = {
    val url = loader.getResource(PhoneToken.RESOURCE_PATH + "ii.au")
    assert(url != null, "ii.au missing from classpath")
    url.getProtocol match {
      case "file" =>
        val dir = new java.io.File(url.toURI).getParentFile
        val files = dir.listFiles
        assert(files != null, dir)
        files.toIndexedSeq.filter(file => file.isFile && file.getName.endsWith(".au")).map(_.getName)
      case other =>
        fail(s"Cannot list allophones from $other URL $url")
    }
  }

  private case class Au(encoding: Int, sampleRate: Int, channels: Int, dataOffset: Int, dataSize: Int, pcm: Array[Double])

  private def parseAu(file: Array[Byte]): Au = {
    assert(file.length > 24)
    assert(file(0) == '.'.toByte && file(1) == 's'.toByte && file(2) == 'n'.toByte && file(3) == 'd'.toByte)
    val dataOffset = u32(file, 4)
    val dataSize = u32(file, 8)
    val encoding = u32(file, 12)
    val sampleRate = u32(file, 16)
    val channels = u32(file, 20)
    assert(dataOffset >= 24 && dataSize > 0 && dataOffset + dataSize <= file.length)
    val pcm = new Array[Double](dataSize)
    var i = 0
    while (i < dataSize) {
      pcm(i) = muLawDecode(file(dataOffset + i))
      i += 1
    }
    Au(encoding, sampleRate, channels, dataOffset, dataSize, pcm)
  }

  private def payloadDifferences(ii: Array[Byte], iiAu: Au, h: Array[Byte], hAu: Au): Int = {
    val compared = math.min(iiAu.dataSize, hAu.dataSize)
    var diffs = math.abs(iiAu.dataSize - hAu.dataSize)
    var i = 0
    while (i < compared) {
      if (ii(iiAu.dataOffset + i) != h(hAu.dataOffset + i)) diffs += 1
      i += 1
    }
    diffs
  }

  private def slice(pcm: Array[Double], from: Double, until: Double): Array[Double] = {
    val start = (from * pcm.length).toInt
    val end = (until * pcm.length).toInt
    pcm.slice(start, end)
  }

  private def powerSpectrum(pcm: Array[Double]): Array[Double] = {
    val n = pcm.length
    val half = n / 2
    val spectrum = new Array[Double](half + 1)
    var k = 0
    while (k <= half) {
      var re = 0.0
      var im = 0.0
      var i = 0
      while (i < n) {
        val angle = 2.0 * math.Pi * k * i / n
        val window = 0.5 - 0.5 * math.cos(2.0 * math.Pi * i / (n - 1))
        val sample = pcm(i) * window
        re += sample * math.cos(angle)
        im -= sample * math.sin(angle)
        i += 1
      }
      spectrum(k) = re * re + im * im
      k += 1
    }
    spectrum
  }

  private def bandEnergy(spectrum: Array[Double], n: Int, sampleRate: Int, lowHz: Double, highHz: Double): Double = {
    val rate = sampleRate.toDouble
    var acc = 0.0
    var k = 0
    while (k < spectrum.length) {
      val freq = k * rate / n
      if (freq >= lowHz && freq < highHz) acc += spectrum(k)
      k += 1
    }
    acc
  }

  private def muLawDecode(ulaw: Byte): Double = {
    val u = (~ulaw.toInt) & 0xff
    val sign = u & 0x80
    val exponent = (u >> 4) & 0x07
    val mantissa = u & 0x0f
    val magnitude = (((mantissa << 3) + 0x84) << exponent) - 0x84
    if (sign != 0) -magnitude.toDouble else magnitude.toDouble
  }

  private def u32(file: Array[Byte], offset: Int): Int =
    ((file(offset) & 0xff) << 24) |
      ((file(offset + 1) & 0xff) << 16) |
      ((file(offset + 2) & 0xff) << 8) |
      (file(offset + 3) & 0xff)
}
