package org.pico.statsd

import org.pico.statsd.datapoint.{DataPointWritable, Sampleable, Sampling}

/**
  * Describes a client connection to a StatsD server, which may be used to post metrics
  * in the form of counters, timers, and gauges.
  *
  * @author Tom Denley
  */
trait StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  def stop(): Unit

  def send[D: DataPointWritable](d: D): Unit

  def sample[S: Sampleable: Sampling](s: S): Unit

  def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: Seq[String], m: Metric[A])(value: A): Unit
}
