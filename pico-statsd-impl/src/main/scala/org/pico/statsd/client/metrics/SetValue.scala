package org.pico.statsd.client.metrics

import org.pico.statsd.client.Metric
import org.pico.statsd.syntax.show._

case class SetValue(aspect: String, value: String, tags: Seq[String])

object SetValue {
  implicit val setValue_Metric_kWoyk3d = new Metric[SetValue] {
    override def encodeMetric(metric: SetValue, prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${metric.value}|s${tagger(metric.tags)}"
    }
  }
}
