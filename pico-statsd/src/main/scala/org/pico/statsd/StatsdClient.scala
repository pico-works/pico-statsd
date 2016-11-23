package org.pico.statsd

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

  /**
    * Adjusts the specified counter by a given delta.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to adjust
    * @param delta
    * the amount to adjust the counter by
    * @param tags
    * array of tags to be added to the data
    */
  def count(aspect: String, delta: Long, tags: String*): Unit

  /**
    * Adjusts the specified counter by a given delta.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to adjust
    * @param delta
    * the amount to adjust the counter by
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def count(aspect: String, delta: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Increments the specified counter by one.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to increment
    * @param tags
    * array of tags to be added to the data
    */
  def incrementCounter(aspect: String, tags: String*): Unit

  /**
    * Increments the specified counter by one.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to increment
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def incrementCounter(aspect: String, sampleRate: SampleRate, tags: String*): Unit

  def increment(aspect: String, tags: String*): Unit

  def increment(aspect: String, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Decrements the specified counter by one.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to decrement
    * @param tags
    * array of tags to be added to the data
    */
  def decrementCounter(aspect: String, tags: String*): Unit

  /**
    * Decrements the specified counter by one.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to decrement
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def decrementCounter(aspect: String, sampleRate: SampleRate, tags: String*): Unit

  def decrement(aspect: String, tags: String*): Unit

  def decrement(aspect: String, sampleRate: SampleRate, tags: String*): Unit

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

  def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: Seq[String], m: Metric[A])(value: A): Unit
}
