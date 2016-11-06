package org.pico.statsd.client.metrics

import org.pico.statsd.client.{Metric, NumberFormatters}
import org.pico.statsd.syntax.show._

case class Histogram[A](aspect: String, value: A, tags: Seq[String])

object Histogram {
  implicit val gauge_Long_Metric_3qR2szH = new Metric[Histogram[Long]] {
    override def encodeMetric(metric: Histogram[Long], prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${NumberFormatters.get.format(metric.value)}|h${tagger(metric.tags)}"
    }
  }

  implicit val gauge_Double_Metric_3qR2szH = new Metric[Histogram[Double]] {
    override def encodeMetric(metric: Histogram[Double], prefix: String, tagger: Seq[String] => String): String = {
      show"$prefix${metric.aspect}:${metric.value}|h${tagger(metric.tags)}"
    }
  }
}
