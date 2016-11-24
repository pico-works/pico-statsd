package org.pico.statsd.syntax.event

import org.pico.disposal.Auto
import org.pico.disposal.std.autoCloseable._
import org.pico.event.Bus
import org.pico.statsd.{NonBlockingStatsdClient, SampleRate}

import scala.concurrent.duration.Deadline

object PerformanceMain {
  def main(args: Array[String]): Unit = {
    for (statsd <- Auto(new NonBlockingStatsdClient("attackstream-dedup", "localhost", 8125))) {
      implicit val statsdInstance = statsd

      val bus = Bus[Unit]

      bus.withCounter("consumer.record.count", SampleRate.always)

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(())
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
