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
  def config: StatsdConfig

  def messages: Source[ByteBuffer]

  def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit

  final def send[D: Printable](config: StatsdConfig, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    send(config.aspect, metric, sampleRate, d, tags)
  }

  final def send[D: Printable](config: StatsdConfig, metric: String, d: D, tags: Seq[String]): Unit = {
    send(config, metric, config.sampleRate, d, tags)
  }

  def sample[A: Metric](a: A): Unit = {
    if (validSample(Metric.of[A].configure(config).sampleRate)) {
      Metric.of[A].sendIn(this, a)
    }
  }

  final def sampledAt(sampleRate: SampleRate): StatsdClient = configured(config.copy(sampleRate = sampleRate))

  final def withAspect(aspect: String): StatsdClient = configured(config.copy(aspect = aspect))

  protected def validSample(sampleRate: SampleRate): Boolean = {
    !(sampleRate.value != 1 && Math.random > sampleRate.value)
  }

  def configured(config: StatsdConfig): StatsdClient
}
