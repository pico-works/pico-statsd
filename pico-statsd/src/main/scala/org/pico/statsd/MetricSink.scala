package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.syntax.metric._

object MetricSink {
  def apply[A](metric: Metric[A], metrics: Metric[A]*)(implicit statsdClient: StatsdClient): Sink[A] = {
    val combinedMetric = (metric /: metrics)(_ :+: _)

    Sink[A](statsdClient.sample(_)(combinedMetric))
  }

  def apply[A]()(implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    Sink[A](statsdClient.sample(_)(metric))
  }

  def apply[A](aspect: String, metric: Metric[A], metrics: Metric[A]*)(implicit statsdClient: StatsdClient): Sink[A] = {
    val combinedMetric = (metric /: metrics)(_ :+: _).inAspect(aspect)

    Sink[A](statsdClient.sample(_)(combinedMetric))
  }

  def apply[A](aspect: String)(implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    val modifiedMetric = metric.inAspect(aspect)

    Sink[A](statsdClient.sample(_)(modifiedMetric))
  }
}
