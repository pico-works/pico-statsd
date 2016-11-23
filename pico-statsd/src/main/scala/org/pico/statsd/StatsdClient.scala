package org.pico.statsd

import com.timgroup.statsd.{Event, ServiceCheck}

/**
  * Describes a client connection to a StatsD server, which may be used to post metrics
  * in the form of counters, timers, and gauges.
  *
  * <p>Three key methods are provided for the submission of data-points for the application under
  * scrutiny:
  * <ul>
  * <li>{@link #incrementCounter} - adds one to the value of the specified named counter</li>
  * <li>{@link #recordGaugeValue} - records the latest fixed value for the specified named gauge</li>
  * <li>{@link #recordExecutionTime} - records an execution time in milliseconds for the specified named operation</li>
  * </ul>
  *
  * @author Tom Denley
  *
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

  /**
    * Convenience method equivalent to {@link #incrementCounter(String, String[])}.
    */
  def increment(aspect: String, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #incrementCounter(String, double, String[])}.
    */
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

  /**
    * Convenience method equivalent to {@link #decrementCounter(String, String[])}.
    */
  def decrement(aspect: String, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #decrementCounter(String, double, String[])}.
    */
  def decrement(aspect: String, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param value
    * the new reading of the gauge
    */
  def recordGaugeValue(aspect: String, value: Double, tags: String*): Unit

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param sampleRate
    * percentage of time metric to be sent
    * @param value
    * the new reading of the gauge
    */
  def recordGaugeValue(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, double, String[])}.
    */
  def gauge(aspect: String, value: Double, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, double, double, String[])}.
    */
  def gauge(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param value
    * the new reading of the gauge
    */
  def recordGaugeValue(aspect: String, value: Long, tags: String*): Unit

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param sampleRate
    * percentage of time metric to be sent
    * @param value
    * the new reading of the gauge
    */
  def recordGaugeValue(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, long, String[])}.
    */
  def gauge(aspect: String, value: Long, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, long, double, String[])}.
    */
  def gauge(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records an execution time in milliseconds for the specified named operation.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the timed operation
    * @param timeInMs
    * the time in milliseconds
    * @param tags
    * array of tags to be added to the data
    */
  def recordExecutionTime(aspect: String, timeInMs: Long, tags: String*): Unit

  /**
    * Records an execution time in milliseconds for the specified named operation.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the timed operation
    * @param timeInMs
    * the time in milliseconds
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def recordExecutionTime(aspect: String, timeInMs: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordExecutionTime(String, long, String[])}.
    */
  def time(aspect: String, value: Long, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordExecutionTime(String, long, double, String[])}.
    */
  def time(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records a value for the specified named histogram.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the histogram
    * @param value
    * the value to be incorporated in the histogram
    * @param tags
    * array of tags to be added to the data
    */
  def recordHistogramValue(aspect: String, value: Double, tags: String*): Unit

  /**
    * Records a value for the specified named histogram.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the histogram
    * @param value
    * the value to be incorporated in the histogram
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def recordHistogramValue(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, double, String[])}.
    */
  def histogram(aspect: String, value: Double, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, double, double, String[])}.
    */
  def histogram(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records a value for the specified named histogram.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the histogram
    * @param value
    * the value to be incorporated in the histogram
    * @param tags
    * array of tags to be added to the data
    */
  def recordHistogramValue(aspect: String, value: Long, tags: String*): Unit

  /**
    * Records a value for the specified named histogram.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the histogram
    * @param value
    * the value to be incorporated in the histogram
    * @param sampleRate
    * percentage of time metric to be sent
    * @param tags
    * array of tags to be added to the data
    */
  def recordHistogramValue(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, long, String[])}.
    */
  def histogram(aspect: String, value: Long, tags: String*): Unit

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, long, double, String[])}.
    */
  def histogram(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit

  /**
    * Records an event
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param event
    * The event to record
    * @param tags
    * array of tags to be added to the data
    * @see <a href="http://docs.datadoghq.com/guides/dogstatsd/#events-1">http://docs.datadoghq.com/guides/dogstatsd/#events-1</a>
    */
  def recordEvent(event: Event, tags: String*): Unit

  /**
    * Records a run status for the specified named service check.
    *
    * @param sc
    * the service check object
    */
  def recordServiceCheckRun(sc: ServiceCheck): Unit

  /**
    * Convenience method equivalent to {@link #recordServiceCheckRun(ServiceCheck sc)}.
    */
  def serviceCheck(sc: ServiceCheck): Unit

  /**
    * Records a value for the specified set.
    *
    * Sets are used to count the number of unique elements in a group. If you want to track the number of
    * unique visitor to your site, sets are a great way to do that.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the set
    * @param value
    * the value to track
    * @param tags
    * array of tags to be added to the data
    * @see <a href="http://docs.datadoghq.com/guides/dogstatsd/#sets">http://docs.datadoghq.com/guides/dogstatsd/#sets</a>
    */
  def recordSetValue(aspect: String, value: String, tags: String*): Unit

  def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: List[String], m: Metric[A])(value: A): Unit
}
