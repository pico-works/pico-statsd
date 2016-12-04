package org.pico.statsd.syntax.event

import org.pico.event.syntax.sinkSource._
import org.pico.event.syntax.source._
import org.pico.event.{Bus, TimedBus}
import org.pico.statsd._
import org.specs2.mutable.Specification

class TimersSpec extends Specification {
  implicit val statsd = NoopStatsdClient

  "Ensuring messages go through" >> {
    "with SinkSource" in {
      val bus = Bus[Int].via(TimedBus(MetricSink(TimerMetric("bus.test"), SampledAt(SampleRate.always))))
      val sum = bus.foldRight(0)(_ + _)
      
      (1 to 10 ).foreach(bus.publish)
      
      sum.value ==== 55
    }
  
    "with Source" in {
      val bus = Bus[Int]
      val sum = bus.map(x => x + 1).via(TimedBus(MetricSink(TimerMetric("bus.test"), SampledAt(SampleRate.always)))).foldRight(0)(_ + _)
    
      (1 to 10 ).foreach(bus.publish)
    
      sum.value ==== 65
    }
  }
}
