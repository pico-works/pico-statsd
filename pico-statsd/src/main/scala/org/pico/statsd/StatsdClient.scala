package org.pico.statsd

import java.io.Closeable

import org.pico.statsd.datapoint.Sampler
import org.pico.statsd.impl.Printable

/**
  * Describes a client connection to a StatsD server, which may be used to post metrics
  * in the form of counters, timers, and gauges.
  *
  * @author Tom Denley
  */
trait StatsdClient extends Closeable {
  def sampleRate: SampleRate

  def send[D: Printable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit

  final def send[D: Printable](aspect: String, d: D, tags: Seq[String]): Unit = {
    send(aspect, sampleRate, d, tags)
  }

  def sample[S: Sampler](s: S): Unit

  def sampledAt(sampleRate: SampleRate): StatsdClient

  protected def validSample(sampleRate: SampleRate): Boolean = {
    !(sampleRate.value != 1 && Math.random > sampleRate.value)
  }
}
