package org.pico.statsd

import org.pico.statsd.datapoint.{DataPoints, Sampling}

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

  def gauge(aspect: String, value: Double, tags: String*): Unit

  def gauge(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  def gauge(aspect: String, value: Long, tags: String*): Unit

  def gauge(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  def time(aspect: String, value: Long, tags: String*): Unit

  def time(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  def histogram(aspect: String, value: Double, tags: String*): Unit

  def histogram(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  def histogram(aspect: String, value: Long, tags: String*): Unit

  def histogram(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  def send[D: DataPoints: Sampling](d: D): Unit

  def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: Seq[String], m: Metric[A])(value: A): Unit
}
