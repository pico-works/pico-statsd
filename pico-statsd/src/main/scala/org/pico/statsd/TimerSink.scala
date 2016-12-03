package org.pico.statsd

import org.pico.event.Sink

import scala.concurrent.duration.Duration

object TimerSink {
  def apply(metricName: String)(implicit statsdClient: StatsdClient): Sink[Duration] = {
    val metric = TimerMetric(metricName)

    Sink[Duration] { duration =>
      statsdClient.sample(duration)(metric)
    }
  }
}
