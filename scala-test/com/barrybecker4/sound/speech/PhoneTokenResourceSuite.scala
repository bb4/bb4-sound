package com.barrybecker4.sound.speech

import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.util.Locale

/** Classpath resolution only. Does not open a mixer or play audio. */
class PhoneTokenResourceSuite extends AnyFunSuite {

  private val loader = getClass.getClassLoader

  private val phrases = Seq(
    "p|l|ee|z e|n|t|er aa nn|u|m|b|er .|.",
    "y|ouu c|a|nn a|d aa s|i|n|ggg|ll w|o|r|dd p|ar|a|m|e|tt|er .|."
  ) ++ Seq("w|u|d", "y|ouu", "l|ii|k", "t|ouu", "p|l|ay", "aa", "gg|AY|M")

  test("documented phrases resolve to canonical allophone resources") {
    val tokens = phrases.flatMap(_.split(" ")).flatMap(_.split("\\|", -1)).distinct
      .filterNot(t => t.isEmpty || t == "," || t == ".")
    val missing = tokens.flatMap { token =>
      val expected = PhoneToken.RESOURCE_PATH + token.toLowerCase(Locale.ROOT) + ".au"
      val resolved = PhoneToken.allophoneResource(token)
      if (resolved.contains(expected) && isAu(expected)) None
      else Some(s"$token -> ${resolved.getOrElse("missing")} (expected $expected)")
    }
    assert(missing.isEmpty, missing.mkString("missing: ", ", ", ""))
  }

  test("legacy underscore spellings resolve to the unsuffixed recording") {
    val aliases = Seq(
      "AA_" -> "aa",
      "ay_" -> "ay",
      "D_" -> "d",
      "GG_" -> "gg",
      "ggg_" -> "ggg",
      "K_" -> "k",
      "l_" -> "l",
      "OUU_" -> "ouu",
      "P_" -> "p",
      "t_" -> "t",
      "U_" -> "u",
      "W_" -> "w",
      "Y_" -> "y"
    )
    aliases.foreach { case (token, stem) =>
      val expected = PhoneToken.RESOURCE_PATH + stem + ".au"
      assertResult(Some(expected))(PhoneToken.allophoneResource(token))
      assert(isAu(expected), expected)
    }
    assertResult(Some(PhoneToken.RESOURCE_PATH + "gg.au"))(PhoneToken.allophoneResource("GG"))
    assertResult(Some(PhoneToken.RESOURCE_PATH + "g.au"))(PhoneToken.allophoneResource("G"))
  }

  test("stem match keeps underscores that are not aliases") {
    assertResult(Some(PhoneToken.RESOURCE_PATH + "h_.au"))(PhoneToken.allophoneResource("H_"))
    assertResult(Some(PhoneToken.RESOURCE_PATH + "h.au"))(PhoneToken.allophoneResource("h"))
    assertResult(Some(PhoneToken.RESOURCE_PATH + "hh_.au"))(PhoneToken.allophoneResource("HH_"))
    // hh_.au is a different stem from the catalog token hh. Do not strip the underscore.
    assertResult(None)(PhoneToken.allophoneResource("hh"))
    assertResult(None)(PhoneToken.allophoneResource("no-such"))
    assert(loader.getResource(PhoneToken.RESOURCE_PATH + "ii.au") != null)
    assert(loader.getResource(PhoneToken.RESOURCE_PATH + "h_.au") != null)
  }

  test("allophone filenames are lowercase and case-unique") {
    val names = auFileNames
    val stems = names.map(_.stripSuffix(".au"))
    val mixedCase = stems.filter(stem => stem != stem.toLowerCase(Locale.ROOT))
    assert(mixedCase.isEmpty, mixedCase.mkString("not lowercase: ", ", ", ""))
    val dupes = stems.groupBy(_.toLowerCase(Locale.ROOT)).filter(_._2.size > 1)
    assert(dupes.isEmpty, dupes.keys.mkString("case-insensitive duplicates: ", ", ", ""))
    assert(!names.contains("A.au"))
    assert(!names.contains("AA_.au"))
    assert(names.contains("a.au"))
    assert(names.contains("ay.au"))
  }

  private def isAu(path: String): Boolean = {
    val url = loader.getResource(path)
    if (url == null) false
    else {
      val in = url.openStream()
      try {
        val magic = in.readNBytes(4)
        new String(magic, StandardCharsets.US_ASCII) == ".snd"
      } finally in.close()
    }
  }

  private def auFileNames: Seq[String] = {
    val sample = loader.getResource(PhoneToken.RESOURCE_PATH + "aa.au")
    assert(sample != null, "aa.au missing from classpath")
    sample.getProtocol match {
      case "file" =>
        val dir = new java.io.File(sample.toURI).getParentFile
        dir.listFiles.filter(f => f.isFile && f.getName.endsWith(".au")).map(_.getName).toIndexedSeq
      case "jar" =>
        val spec = sample.getPath
        val bang = spec.indexOf('!')
        val jarFile = new java.io.File(java.net.URI.create(spec.substring(0, bang)))
        val prefix = PhoneToken.RESOURCE_PATH
        val jar = new java.util.jar.JarFile(jarFile)
        try {
          val names = scala.collection.mutable.ListBuffer.empty[String]
          val entries = jar.entries()
          while (entries.hasMoreElements) {
            val name = entries.nextElement().getName
            if (name.startsWith(prefix) && name.endsWith(".au") && !name.substring(prefix.length).contains("/"))
              names += name.substring(prefix.length)
          }
          names.toSeq
        } finally jar.close()
      case other =>
        fail(s"Cannot list allophones from $other URL $sample")
    }
  }
}
