package org.pico.statsd.datapoint

import org.pico.statsd.SampleRate

trait Sampling[A] {
  def sampleRate(a: A): SampleRate
}

object Sampling {
  def apply[A](value: SampleRate): Sampling[A] = {
    new Sampling[A] {
      override def sampleRate(a: A): SampleRate = value
    }
  }

  def of[D: Sampling]: Sampling[D] = implicitly[Sampling[D]]
}
