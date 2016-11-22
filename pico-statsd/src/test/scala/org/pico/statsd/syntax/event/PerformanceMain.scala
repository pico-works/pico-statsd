package org.pico.statsd.syntax.event

import com.timgroup.statsd.StatsDClient
import org.pico.disposal.{Auto, Disposable}
import org.pico.event.Bus
import org.pico.statsd.PicoStatsDClient

import scala.concurrent.duration.Deadline

object PerformanceMain {
  implicit val statsDClientDisposable = Disposable[StatsDClient](_.stop())

  def main(args: Array[String]): Unit = {
    for (statsd <- Auto(new PicoStatsDClient("attackstream-dedup", "localhost", 8125))) {
      implicit val statsdInstance = statsd

      val bus = Bus[Unit]

      bus.withCounter("consumer.record.count", None)

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(())
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
