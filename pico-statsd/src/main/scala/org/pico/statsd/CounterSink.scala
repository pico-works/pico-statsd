package org.pico.statsd

import org.pico.event.Sink

object CounterSink {
  def apply[A](metric: String)(implicit statsdClient: StatsdClient): Sink[A] = {
    val sampler = CountMetric[Increment](metric)

    Sink[A] { a =>
      statsdClient.sample(Increment())(sampler)
    }
  }
}
