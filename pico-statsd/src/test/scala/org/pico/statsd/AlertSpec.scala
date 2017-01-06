package org.pico.statsd

import org.pico.disposal.Auto
import org.pico.disposal.std.autoCloseable._
import org.pico.event.Bus
import org.pico.statsd.arb._
import org.pico.statsd.datapoint._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.pico.statsd.syntax.event._

class AlertSpec extends Specification with ScalaCheck {
  sequential
  "Should send datagrams" >> {
    implicit val stringAlert = Alert[String](Event("eventTitle").comap(txt => EventData(txt, Priority.Low, AlertType.Success)))
  
    "via withAlert" in prop { (prefix: Identifier, aspect: Identifier, txts: List[String]) =>
      implicit val client = TestStastDClient(prefix.value, SampleRate.always, "name:John", "lastname:Doe")
  
      for {
        bus <- Auto(Bus[String])
        _   <- Auto(bus.withAlert(aspect.value, "extraTag"))
      } {
        txts.foreach(bus.publish)
      }
    
      val expected = txts.map { txt =>
        val title = List(prefix, aspect, Identifier("eventTitle")).map(_.value).filter(_.nonEmpty).mkString(".")
        s"_e{${title.length},${txt.length}}:$title|$txt|p:low|t:success|#name:John,lastname:Doe,extraTag"
      }
    
      client.sentMessages.value.reverse ==== expected
    }
  
    "always via generic sink" in prop { txts: List[Identifier] =>
      implicit val client = TestStastDClient("prefix", SampleRate(0.01), "name:John", "lastname:Doe")
  
      for {
        bus <- Auto(Bus[String])
        _   <- Auto(bus.withAlert("aspect", "extraTag"))
      } {
        txts.map(_.value).foreach(bus.publish)
      }
    
      val title = "prefix.aspect.eventTitle"
      val expected = txts.map(_.value).map { txt =>
        s"_e{${title.length},${txt.length}}:$title|$txt|p:low|t:success|#name:John,lastname:Doe,extraTag"
      }
      
      val actual = client.sentMessages.value.reverse
      
      actual.length ==== expected.length
  
      actual ==== expected
    }
    
    "always via specific sink" in prop { txts: List[EventData] =>
      implicit val client = TestStastDClient("prefix", SampleRate(0.01), "name:John", "lastname:Doe")
  
      for {
        bus <- Auto(Bus[EventData])
        _   <- Auto(bus into eventSink("aspect.eventTitle", "extraTag"))
      } {
        txts.foreach(bus.publish)
      }
  
      val title = "prefix.aspect.eventTitle"
      val expected = txts.map { e =>
        s"_e{${title.length},${e.text.length}}:$title|${e.text}|p:${e.priority.toString.toLowerCase}|t:${e.alertType.toString.toLowerCase}|#name:John,lastname:Doe,extraTag"
      }
  
      val actual = client.sentMessages.value.reverse
      actual.length ==== expected.length
      actual ==== expected
    }
  
  }
}

