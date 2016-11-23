package org.pico.statsd.syntax.event

import org.pico.disposal.{Auto, Disposable}
import org.pico.event.Bus
import org.pico.statsd.{NonBlockingStatsdClient, SampleRate, StatsdClient}

import scala.concurrent.duration.Deadline

object PerformanceMain {
  implicit val statsDClientDisposable = Disposable[StatsdClient](_.stop())

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
