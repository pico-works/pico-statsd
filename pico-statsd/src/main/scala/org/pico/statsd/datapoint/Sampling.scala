package org.pico.statsd.datapoint

import org.pico.statsd.SampleRate

trait Sampling[A] {
  def sampleRate(a: A): SampleRate
}

object Sampling {
  def of[D: Sampling]: Sampling[D] = implicitly[Sampling[D]]
}
