package org.pico.statsd

import org.pico.statsd.datapoint.{DataPointWritable, Sampler}

/**
  * Describes a client connection to a StatsD server, which may be used to post metrics
  * in the form of counters, timers, and gauges.
  *
  * @author Tom Denley
  */
trait StatsdClient {
  def sampleRate: SampleRate

  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  def stop(): Unit

  def send[D: DataPointWritable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit

  final def send[D: DataPointWritable](aspect: String, d: D, tags: Seq[String]): Unit = {
    send(aspect, sampleRate, d, tags)
  }

  def sample[S: Sampler](s: S): Unit

  def sampledAt(sampleRate: SampleRate): StatsdClient

  protected def validSample(sampleRate: SampleRate): Boolean = {
    !(sampleRate.value != 1 && Math.random > sampleRate.value)
  }
}
