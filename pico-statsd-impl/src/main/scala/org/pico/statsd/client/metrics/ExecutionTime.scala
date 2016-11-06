package org.pico.statsd.client.metrics

import org.pico.statsd.client.Metric
import org.pico.statsd.syntax.show._

case class ExecutionTime(aspect: String, timeInMs: Long, tags: Seq[String])

object ExecutionTime {
  implicit val metric_Counter_d4RaaNY = new Metric[ExecutionTime] {
    override def encodeMetric(metric: ExecutionTime, prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${metric.timeInMs}|ms${tagger(metric.tags)}"
    }
  }
}
