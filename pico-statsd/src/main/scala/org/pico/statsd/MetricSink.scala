package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.datapoint.Metric

object MetricSink {
  def apply[A](metric: Metric[A])(implicit statsdClient: StatsdClient): Sink[A] = {
    Sink[A] { a =>
      statsdClient.sample(a)(metric)
    }
  }
}
