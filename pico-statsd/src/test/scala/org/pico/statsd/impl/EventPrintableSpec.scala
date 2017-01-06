package org.pico.statsd.impl

import java.io.{PrintWriter, StringWriter}

import org.pico.statsd.SampleRate
import org.pico.statsd.arb._
import org.pico.statsd.datapoint.EventData
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class EventPrintableSpec extends Specification with ScalaCheck {
  
  "Should construct datagram" in prop { (aspect: Identifier, title: Identifier, evt: EventData, tag1: Identifier, tag2: Identifier) =>
    val fullTitle = List(aspect.value, title.value).filterNot(_.isEmpty).mkString(".")
    val tags = List(tag1, tag2).map(_.value).filterNot(_.isEmpty).mkString(",")
    val expected = s"_e{${fullTitle.length},${evt.text.length}}:$fullTitle|${evt.text}|p:${evt.priority.toString.toLowerCase()}|t:${evt.alertType.toString.toLowerCase}|#$tags"

    val strWriter = new StringWriter()
    val out = new PrintWriter(strWriter)
  
    Printable.of[EventData]
      .write(out, aspect.value, title.value, SampleRate.always, evt) { tagWriter =>
        List(tag1, tag2).map(_.value).foreach(tagWriter.writeTag)
      }
    
    out.close()
    
    strWriter.toString ==== expected
    
  }
}
