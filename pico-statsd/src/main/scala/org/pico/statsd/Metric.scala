package org.pico.statsd

import scala.concurrent.duration.FiniteDuration

sealed trait MetricValue
case class IntegralGauge(aspect: String, value: Long)         extends MetricValue
case class FractionalGauge(aspect: String, value: Double)     extends MetricValue
case class IntegralHistogram(aspect: String, value: Long)     extends MetricValue
case class FractionalHistogram(aspect: String, value: Double) extends MetricValue
case class Counter(aspect: String, value: Long)               extends MetricValue
case class Timer(aspect: String, value: FiniteDuration)       extends MetricValue
