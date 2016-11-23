package org.pico.statsd.syntax.event

import org.pico.disposal.{Auto, Disposable}
import org.pico.event.Bus
import org.pico.statsd._

import scala.concurrent.duration.Deadline

object MetricPerformanceMain {
  implicit val statsDClientDisposable = Disposable[StatsdClient](_.stop())

  case class Topic(name: String) extends AnyVal
  case class Record(topic: Topic, partition: Long, offset: Long)

  implicit def consumedRecordMetric[A]: Metric[Record] =
    Metric[Record](
      r => List(IntegralGauge("offset", r.offset), Counter("record.count", 1)),
      x => List("topic:" + x.topic.name, "partition:" + x.partition))

  def main(args: Array[String]): Unit = {
    val record = Record(Topic("topic"), 1L, 1000L)

    for (statsd <- Auto(new NonBlockingStatsdClient("attackstream-dedup", "localhost", 8125))) {
      implicit val statsdInstance = statsd

      val bus = Bus[Record]

      bus.withMetrics("consumer.record.count", None)

      val before = Deadline.now

      for (i <- 0 until 10000000) {
        bus.publish(record)
      }

      val after = Deadline.now

      println(after - before)
    }
  }
}
