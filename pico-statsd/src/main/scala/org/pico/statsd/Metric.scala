package org.pico.statsd

trait Metric[A] {
  def tags(value: A): List[String]
  def values(value: A): List[MetricValue]
}

object Metric {
  def apply[A](toValues: A => List[MetricValue], toTags: A => List[String]): Metric[A] = new Metric[A] {
    def values(value: A): List[MetricValue] = toValues(value)
    def tags(value: A): List[String] = toTags(value)
  }
}

sealed trait MetricValue
case class IntegralGauge(aspect: String, value: Long)         extends MetricValue
case class FractionalGauge(aspect: String, value: Double)     extends MetricValue
case class IntegralHistogram(aspect: String, value: Long)     extends MetricValue
case class FractionalHistogram(aspect: String, value: Double) extends MetricValue
case class Counter(aspect: String, value: Long)               extends MetricValue
case class Timer(aspect: String, value: Long)                 extends MetricValue



