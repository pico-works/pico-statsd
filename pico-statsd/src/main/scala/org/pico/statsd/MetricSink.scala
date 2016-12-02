package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.datapoint.Metric

object MetricSink {
  def apply[A](metric: Metric[A])(implicit statsdClient: StatsdClient): Sink[A] = {
    Sink[A](statsdClient.sample(_)(metric))
  }

  def apply[A]()(implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    Sink[A](statsdClient.sample(_)(metric))
  }
}
