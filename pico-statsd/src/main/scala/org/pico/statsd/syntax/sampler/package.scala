package org.pico.statsd.syntax

import org.pico.statsd.datapoint.Metric

package object sampler {
  implicit class SamplerOps_vy4ffYh[A](val self: Metric[A]) extends AnyVal {
    def :+:(that: Metric[A]): Metric[A] = Metric.append(self, that)
  }
}
