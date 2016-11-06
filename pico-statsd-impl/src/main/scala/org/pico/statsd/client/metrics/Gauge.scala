package org.pico.statsd.client.metrics

import org.pico.statsd.client.{Metric, NumberFormatters}
import org.pico.statsd.syntax.show._

case class Gauge[A](aspect: String, value: A, tags: Seq[String])

object Gauge {
  implicit val gauge_Long_Metric_3qR2szH = new Metric[Gauge[Long]] {
    override def encodeMetric(metric: Gauge[Long], prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${metric.value}|g${tagger(metric.tags)}"
    }
  }

  implicit val gauge_Double_Metric_3qR2szH = new Metric[Gauge[Double]] {
    override def encodeMetric(metric: Gauge[Double], prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${NumberFormatters.get.format(metric.value)}|g${tagger(metric.tags)}"
    }
  }
}
