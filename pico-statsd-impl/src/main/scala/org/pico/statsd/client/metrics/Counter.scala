package org.pico.statsd.client.metrics

import org.pico.statsd.client.Metric
import org.pico.statsd.syntax.show._

case class Counter(aspect: String, delta: Long, tags: Seq[String])

case object Counter {
  def increment(aspect: String, tags: Seq[String]): Counter = Counter(aspect, 1L, tags)

  def decrement(aspect: String, tags: Seq[String]): Counter = Counter(aspect, -1L, tags)

  implicit val metric_Counter_d4RaaNY = new Metric[Counter] {
    override def encodeMetric(metric: Counter, prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${metric.delta}|c${tagger(metric.tags)}"
    }
  }
}
