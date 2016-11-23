package org.pico.statsd.datapoint

sealed trait SampleMetricType

case class IntegralGauge(aspect: String)        extends SampleMetricType
case class FractionalGauge(aspect: String)      extends SampleMetricType
case class IntegralHistogram(aspect: String)    extends SampleMetricType
case class FractionalHistogram(aspect: String)  extends SampleMetricType
case class Counter(aspect: String)              extends SampleMetricType
case class Timer(aspect: String)                extends SampleMetricType
