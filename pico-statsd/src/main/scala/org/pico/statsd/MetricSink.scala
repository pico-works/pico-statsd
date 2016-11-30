package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.datapoint.Metric

object MetricSink {
  def apply[A](metric: Metric[A])(implicit statsdClient: StatsdClient): Sink[A] = {
    Sink[A] { a =>
      statsdClient.sample(a)(metric)
    }
  }

  def apply[A](aspect: String, metric: Metric[A])(implicit statsdClient: StatsdClient): Sink[A] = {
    val configuredClient = statsdClient.withAspect(aspect)

    Sink[A] { a =>
      configuredClient.sample(a)(metric)
    }
  }

  def apply[A](aspect: String, metric: Metric[A], sampleRate: SampleRate)(implicit statsdClient: StatsdClient): Sink[A] = {
    val configuredClient = statsdClient.withAspect(aspect).sampledAt(sampleRate)

    Sink[A] { a =>
      configuredClient.sample(a)(metric)
    }
  }

  def apply[A](implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    MetricSink(metric)(statsdClient)
  }

  def apply[A](aspect: String)(implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    MetricSink(statsdClient.withAspect(aspect), metric)
  }

  def apply[A](aspect: String, sampleRate: SampleRate)(implicit statsdClient: StatsdClient, metric: Metric[A]): Sink[A] = {
    MetricSink(statsdClient.withAspect(aspect).sampledAt(sampleRate), metric)
  }
}
