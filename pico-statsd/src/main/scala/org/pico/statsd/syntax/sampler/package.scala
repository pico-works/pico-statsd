package org.pico.statsd.syntax

import org.pico.statsd.datapoint.Sampler

package object sampler {
  implicit class SamplerOps_vy4ffYh[A](val self: Sampler[A]) extends AnyVal {
    def :+:(that: Sampler[A]): Sampler[A] = Sampler.append(self, that)
  }
}
