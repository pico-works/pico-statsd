package org.pico.statsd.client

trait Metric[A] {
  def encodeMetric(metric: A, prefix: String, tagger: Seq[String] => String): String
}

object Metric {
  def of[A: Metric]: Metric[A] = implicitly[Metric[A]]
}
