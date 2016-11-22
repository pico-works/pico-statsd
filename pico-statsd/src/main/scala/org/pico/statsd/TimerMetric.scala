package org.pico.statsd

import com.timgroup.statsd.StatsDClient

import scala.concurrent.duration.FiniteDuration

/**
  * Classifies value as a counter metric.
  * The trait is sealed so only smart constructors can be used to create instances.
  */
sealed trait TimerMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsDClient, aspect: String, value: A, duration: FiniteDuration, sampleRate: Option[Double], extraTags: List[String]): Unit
}

object TimerMetric {
  /**
    * Construct [[TimerMetric]] instance for a given type.
    * Timers can only accept integral values
    * @param toTags maps the value to value specific tags
    */
  def integral[A](toTags: A => List[String]): TimerMetric[A] = new TimerMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, value: A, duration: FiniteDuration, sampleRate: Option[Double], t: List[String]): Unit =
      client.time(aspect, duration.toMillis, sampleRate.getOrElse(1d), t ++ tags(value): _*)
  }
}
