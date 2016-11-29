package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.datapoint.{Increment, CountMetric}

object CounterSink {
  def apply[A](metric: String)(implicit statsdClient: StatsdClient): Sink[A] = {
    val sampler = CountMetric[Increment](metric)

    Sink[A] { a =>
      statsdClient.sample(Increment())(sampler)
    }
  }
}
