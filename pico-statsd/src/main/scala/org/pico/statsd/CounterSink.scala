package org.pico.statsd

import org.pico.event.Sink
import org.pico.statsd.datapoint.{Increment, IncrementMetric}

object CounterSink {
  def apply[A](metric: String)(implicit statsdClient: StatsdClient): Sink[A] = {
    val sampler = IncrementMetric[Increment](metric)

    Sink[A] { a =>
      statsdClient.sample(Increment())(sampler)
    }
  }
}
