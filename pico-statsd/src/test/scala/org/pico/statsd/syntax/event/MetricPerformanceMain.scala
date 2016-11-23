package org.pico.statsd.syntax.event

import org.pico.disposal.{Auto, Disposable}
import org.pico.event.Bus
import org.pico.statsd._
import org.pico.statsd.datapoint._

import scala.concurrent.duration.Deadline

object MetricPerformanceMain {
  implicit val statsDClientDisposable = Disposable[StatsdClient](_.stop())

  case class Topic(name: String) extends AnyVal

  case class Record(topic: Topic, partition: Long, offset: Long)

  implicit val samplerRecord = Sampler[Record](
    IntegralGaugeSampler("offset").comap(_.offset),
    CountSampler("record.count"),
    TaggedBy(v => "topic:" + v.topic.name),
    TaggedBy(v => "partition:" + v.partition))

  def main(args: Array[String]): Unit = {
    val record = Record(Topic("topic"), 1L, 1000L)

    for (statsd <- Auto(new NonBlockingStatsdClient("attackstream-dedup", "localhost", 8125))) {
      implicit val statsdInstance = statsd.sampledAt(SampleRate(0.001))

      val bus = Bus[Record]

      bus.withMetrics("consumer.record.count", SampleRate.always)//(0.001))

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(record)
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
