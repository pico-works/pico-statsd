package org.pico.statsd.datapoint

import org.pico.statsd.StatsdClient

trait Sampleable[A] {
  def sampleIn(client: StatsdClient, a: A): Unit
}

object Sampleable {
  def of[A: Sampleable]: Sampleable[A] = implicitly[Sampleable[A]]
}
