package org.pico.statsd.syntax

import org.pico.statsd.datapoint.{Alert, Metric}

package object sampler {
  implicit class SamplerOps_Metrics_vy4ffYh[A](val self: Metric[A]) extends AnyVal {
    def :+:(that: Metric[A]): Metric[A] = Metric.append(self, that)
  }
  
  implicit class SamplerOps_Events_vy4ffYh[A](val self: Alert[A]) extends AnyVal {
    def :+:(that: Alert[A]): Alert[A] = Alert.append(self, that)
  }
}
