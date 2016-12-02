package org.pico.statsd.syntax.event

import org.pico.disposal.std.autoCloseable._
import org.pico.disposal.{Auto, Disposable}
import org.pico.event.Bus
import org.pico.event.syntax.sinkSource._
import org.pico.statsd._
import org.pico.statsd.datapoint._
import org.pico.statsd.impl.{StaticAddressResolution, UdpEmitter}
import org.pico.statsd.syntax.metric._

import scala.concurrent.duration.Deadline

object MetricPerformanceMain {
  implicit val statsDClientDisposable = Disposable[StatsdClient](_.close())

  case class Topic(name: String) extends AnyVal

  case class Record(topic: Topic, partition: Long, offset: Long)

  implicit val samplerRecord = Metric[Record](
    IntegralGaugeMetric("offset").comap(v => LongGauge(v.offset)),
    CountMetric("record.count"),
    TaggedBy(v => "topic:" + v.topic.name),
    TaggedBy(v => "partition:" + v.partition))

  def main(args: Array[String]): Unit = {
    val record = Record(Topic("topic"), 1L, 1000L)

    for {
      statsd            <- Auto(NonBlockingStatsdClient("attackstream-dedup", 1000000, Array("club_name:moo")))
      udpEmitter        <- Auto(UdpEmitter(StaticAddressResolution("localhost", 8125)))
      _                 <- Auto(statsd.messages into udpEmitter)
//      _           <- Auto(statsd.messages.subscribe(b => println(new String(b.array(), 0, b.limit()))))
    } {
      implicit val statsdInstance = statsd.sampledAt(SampleRate(0.001))

      val bus = Bus[Record]

      bus.tap(MetricSink(Metric() :+: InAspect("consumer") :+: SampledAt(SampleRate(0.001))))

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(record)
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
