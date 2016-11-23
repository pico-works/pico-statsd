package org.pico.statsd.syntax.event

import org.pico.event.Bus
import org.pico.statsd.NoopStatsdClient
import org.specs2.mutable.Specification

class TimersSpec extends Specification {
  implicit val statsd = NoopStatsdClient

  "Ensuring messages go through" >> {
    "with SinkSource" in {
      val bus = Bus[Int].withSimpleTimer("bus.test", None)
      val sum = bus.foldRight(0)(_ + _)
      
      (1 to 10 ).foreach(bus.publish)
      
      sum.value ==== 55
    }
  
    "with Source" in {
      val bus = Bus[Int]
      val sum = bus.map(x => x + 1).withSimpleTimer("bus.test", None).foldRight(0)(_ + _)
    
      (1 to 10 ).foreach(bus.publish)
    
      sum.value ==== 65
    }
  }
}
