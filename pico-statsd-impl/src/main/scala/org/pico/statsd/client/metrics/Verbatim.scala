package org.pico.statsd.client.metrics

import org.pico.statsd.client.Metric

case class Verbatim(text: String) extends AnyVal

object Verbatim {
  implicit val verbatim_Metric_3qR2szH = new Metric[Verbatim] {
    override def encodeMetric(metric: Verbatim, prefix: String, tagger: Seq[String] => String): String = {
      metric.text
    }
  }
}
