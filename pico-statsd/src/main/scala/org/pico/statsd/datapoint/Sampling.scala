package org.pico.statsd.datapoint

import org.pico.statsd.SampleRate

trait Sampling[A] {
  def sampleRate(a: A): SampleRate

  def writeSampleRate(sb: StringBuilder, a: A): Unit
}

object Sampling {
  def of[D: Sampling]: Sampling[D] = implicitly[Sampling[D]]
}
