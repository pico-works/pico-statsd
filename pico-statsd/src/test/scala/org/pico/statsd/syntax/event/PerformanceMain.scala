package org.pico.statsd.syntax.event

import org.pico.disposal.Auto
import org.pico.disposal.std.autoCloseable._
import org.pico.event.Bus
import org.pico.statsd.impl.{Inet, UdpEmitter}
import org.pico.statsd.{NonBlockingStatsdClient, SampleRate}

import scala.concurrent.duration.Deadline

object PerformanceMain {
  def main(args: Array[String]): Unit = {
    for {
      statsd      <- Auto(new NonBlockingStatsdClient("attackstream-dedup"))
      udpEmitter  <- Auto(UdpEmitter(Inet.staticStatsdAddressResolution("localhost", 8125)))
      _           <- Auto(statsd.messages into udpEmitter)
      _           <- Auto(statsd.messages.subscribe(b => println(new String(b.array(), 0, b.limit()))))
    } {
      implicit val statsdInstance = statsd

      val bus = Bus[Unit]

      bus.withCounter("consumer.record.count", SampleRate.always, "helloworld")

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(())
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
