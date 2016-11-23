package org.pico.statsd

import scala.concurrent.duration.FiniteDuration

/**
  * Classifies value as a counter metric.
  * The trait is sealed so only smart constructors can be used to create instances.
  */
sealed trait TimerMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsdClient, aspect: String, value: A, duration: FiniteDuration, sampleRate: Option[SampleRate], extraTags: List[String]): Unit
}

object TimerMetric {
  /**
    * Construct [[TimerMetric]] instance for a given type.
    * Timers can only accept integral values
    * @param toTags maps the value to value specific tags
    */
  def integral[A](toTags: A => List[String]): TimerMetric[A] = new TimerMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, value: A, duration: FiniteDuration, sampleRate: Option[SampleRate], t: List[String]): Unit =
      client.time(aspect, duration.toMillis, sampleRate.getOrElse(SampleRate.always), t ++ tags(value): _*)
  }
}
