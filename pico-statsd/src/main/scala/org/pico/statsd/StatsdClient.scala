package org.pico.statsd

import java.io.Closeable
import java.nio.ByteBuffer

import org.pico.event.Source
import org.pico.statsd.datapoint.Metric
import org.pico.statsd.impl.Printable

/**
  * Describes a client connection to a StatsD server, which may be used to post metrics
  * in the form of counters, timers, and gauges.
  */
trait StatsdClient extends Closeable {
  def aspect: String

  def messages: Source[ByteBuffer]

  def sampleRate: SampleRate

  def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit

  final def send[D: Printable](metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    send(aspect, metric, sampleRate, d, tags)
  }

  final def send[D: Printable](metric: String, d: D, tags: Seq[String]): Unit = {
    send(metric, sampleRate, d, tags)
  }

  def sample[A: Metric](a: A): Unit

  def sampledAt(sampleRate: SampleRate): StatsdClient

  def withAspect(aspect: String): StatsdClient

  protected def validSample(sampleRate: SampleRate): Boolean = {
    !(sampleRate.value != 1 && Math.random > sampleRate.value)
  }
}
