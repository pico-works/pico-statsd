package org.pico.statsd

import java.lang.{StringBuilder => JStringBuilder}
import java.net.InetSocketAddress
import java.text.DecimalFormat
import java.util.concurrent.Callable

import com.timgroup.statsd._

/**
  * Create a new StatsD client communicating with a StatsD instance on the
  * specified host and port. All messages send via this client will have
  * their keys prefixed with the specified string. The new client will
  * attempt to open a connection to the StatsD server immediately upon
  * instantiation, and may throw an exception if that a connection cannot
  * be established. Once a client has been instantiated in this way, all
  * exceptions thrown during subsequent usage are passed to the specified
  * handler and then consumed, guaranteeing that failures in metrics will
  * not affect normal code execution.
  *
  * @param prefix
  * the prefix to apply to keys sent via this client
  * @param constantTags
  * tags to be added to all content sent
  * @param errorHandler
  * handler to use when an exception occurs during usage, may be null to indicate noop
  * @param addressLookup
  * yields the IP address and socket of the StatsD server
  * @param queueSize
  * the maximum amount of unprocessed messages in the BlockingQueue.
  * @throws StatsDClientException
  * if the client could not be started
  */
final class NonBlockingStatsdClient(
    val prefix: String = "",
    val queueSize: Int,
    var constantTags: Array[String] = null,
    val errorHandler: StatsDClientErrorHandler,
    val addressLookup: Callable[InetSocketAddress]) extends StatsdClient {
  // Empty list should be null for faster comparison
  if (constantTags != null && constantTags.isEmpty) {
    constantTags = null
  }

  val constantTagsRendered = if (constantTags != null) {
    val sb = new JStringBuilder()
    Tags.appendTagString(sb, constantTags, null)
    sb.toString
  } else {
    null
  }

  val client = new InternalStatsdClient(queueSize, errorHandler, addressLookup)

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are consumed, guaranteeing
    * that failures in metrics will not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @param queueSize
    * the maximum amount of unprocessed messages in the BlockingQueue.
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int, queueSize: Int) {
    this(
      prefix,
      Integer.MAX_VALUE,
      Array.empty[String],
      new StatsDClientErrorHandler {
        override def handle(exception: Exception): Unit = ()
      },
      Inet.staticStatsDAddressResolution(hostname, port))
  }

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are consumed, guaranteeing
    * that failures in metrics will not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int) {
    this(prefix, hostname, port, Integer.MAX_VALUE)
  }

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are passed to the specified
    * handler and then consumed, guaranteeing that failures in metrics will
    * not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @param constantTags
    * tags to be added to all content sent
    * @param errorHandler
    * handler to use when an exception occurs during usage, may be null to indicate noop
    * @param queueSize
    * the maximum amount of unprocessed messages in the BlockingQueue.
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int, queueSize: Int, constantTags: Array[String], errorHandler: StatsDClientErrorHandler) {
    this(prefix, queueSize, constantTags, errorHandler, Inet.staticStatsDAddressResolution(hostname, port))
  }

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are consumed, guaranteeing
    * that failures in metrics will not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @param constantTags
    * tags to be added to all content sent
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int, constantTags: String*) {
    this(
      prefix,
      Integer.MAX_VALUE,
      constantTags.toArray,
      new StatsDClientErrorHandler {
        override def handle(exception: Exception): Unit = ()
      },
      Inet.staticStatsDAddressResolution(hostname, port))
  }

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are consumed, guaranteeing
    * that failures in metrics will not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @param constantTags
    * tags to be added to all content sent
    * @param queueSize
    * the maximum amount of unprocessed messages in the BlockingQueue.
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int, queueSize: Int, constantTags: String*) {
    this(
      prefix,
      Integer.MAX_VALUE,
      constantTags.toArray,
      new StatsDClientErrorHandler {
        override def handle(exception: Exception): Unit = ()
      },
      Inet.staticStatsDAddressResolution(hostname, port))

  }

  /**
    * Create a new StatsD client communicating with a StatsD instance on the
    * specified host and port. All messages send via this client will have
    * their keys prefixed with the specified string. The new client will
    * attempt to open a connection to the StatsD server immediately upon
    * instantiation, and may throw an exception if that a connection cannot
    * be established. Once a client has been instantiated in this way, all
    * exceptions thrown during subsequent usage are passed to the specified
    * handler and then consumed, guaranteeing that failures in metrics will
    * not affect normal code execution.
    *
    * @param prefix
    * the prefix to apply to keys sent via this client
    * @param hostname
    * the host name of the targeted StatsD server
    * @param port
    * the port of the targeted StatsD server
    * @param constantTags
    * tags to be added to all content sent
    * @param errorHandler
    * handler to use when an exception occurs during usage, may be null to indicate noop
    * @throws StatsDClientException
    * if the client could not be started
    */
  def this(prefix: String, hostname: String, port: Int, constantTags: Array[String], errorHandler: StatsDClientErrorHandler) {
    this(prefix, Integer.MAX_VALUE, constantTags, errorHandler, Inet.staticStatsDAddressResolution(hostname, port))
  }

  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def stop(): Unit = client.stop()

  /**
    * Generate a suffix conveying the given tag list to the client
    */
  private def appendTagString(sb: JStringBuilder, tags: Seq[String]): Unit = {
    Tags.appendTagString(sb, tags, constantTagsRendered)
  }

  val decimalFormat = new DecimalFormat("#.################")

  /**
    * Adjusts the specified counter by a given delta.
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
  override def count(aspect: String, delta: Long, tags: String*): Unit = {
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(delta)
    sb.append("|c")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def count(aspect: String, delta: Long, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      val sb = new JStringBuilder(50)

      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(delta)
      sb.append("|c|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)

      client.send(sb.toString)
    }
  }

  /**
    * Increments the specified counter by one.
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to increment
    * @param tags
    * array of tags to be added to the data
    */
  override def incrementCounter(aspect: String, tags: String*): Unit = {
    count(aspect, 1L, tags: _*)
  }

  override def incrementCounter(aspect: String, sampleRate: SampleRate, tags: String*): Unit = {
    count(aspect, 1L, sampleRate, tags: _*)
  }

  override def increment(aspect: String, tags: String*): Unit = {
    incrementCounter(aspect, tags: _*)
  }

  override def increment(aspect: String, sampleRate: SampleRate, tags: String*): Unit = {
    incrementCounter(aspect, sampleRate, tags: _*)
  }

  /**
    * Decrements the specified counter by one.
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the counter to decrement
    * @param tags
    * array of tags to be added to the data
    */
  override def decrementCounter(aspect: String, tags: String*): Unit = {
    count(aspect, -1, tags: _*)
  }

  override def decrementCounter(aspect: String, sampleRate: SampleRate, tags: String*): Unit = {
    count(aspect, -1, sampleRate, tags: _*)
  }

  override def decrement(aspect: String, tags: String*): Unit = {
    decrementCounter(aspect, tags: _*)
  }

  override def decrement(aspect: String, sampleRate: SampleRate, tags: String*): Unit = {
    decrementCounter(aspect, sampleRate, tags: _*)
  }

  override def gauge(aspect: String, value: Double, tags: String*): Unit = {
    // Intentionally using %s rather than %f here to avoid padding with extra 0s to represent
    // precision

    val sb = new JStringBuilder()
    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(StatsdNumberFormat.get.format(value))
    sb.append("|g")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def gauge(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      val sb = new JStringBuilder()
      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(StatsdNumberFormat.get.format(value))
      sb.append("|g|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)

      client.send(sb.toString)
    }
  }

  override def gauge(aspect: String, value: Long, tags: String*): Unit = {
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(value)
    sb.append("|g")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def gauge(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      val sb = new JStringBuilder()

      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(value)
      sb.append("|g|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)

      client.send(sb.toString)
    }
  }

  override def time(aspect: String, timeInMs: Long, tags: String*): Unit = {
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(timeInMs)
    sb.append("|ms")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def time(aspect: String, timeInMs: Long, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      val sb = new JStringBuilder()
      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(timeInMs)
      sb.append("|ms|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)
      client.send(sb.toString)
    }
  }

  override def histogram(aspect: String, value: Double, tags: String*): Unit = {
    // Intentionally using %s rather than %f here to avoid
    // padding with extra 0s to represent precision
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(StatsdNumberFormat.get.format(value))
    sb.append("|h")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def histogram(aspect: String, value: Double, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      // Intentionally using %s rather than %f here to avoid
      // padding with extra 0s to represent precision
      val sb = new JStringBuilder()

      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(StatsdNumberFormat.get.format(value))
      sb.append("|h|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)

      client.send(sb.toString)
    }
  }

  override def histogram(aspect: String, value: Long, tags: String*): Unit = {
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(value)
    sb.append("|h")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  override def histogram(aspect: String, value: Long, sampleRate: SampleRate, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      val sb = new JStringBuilder()

      sb.append(prefix)
      sb.append(aspect)
      sb.append(":")
      sb.append(value)
      sb.append("|h|@")
      sb.append(sampleRate.text)
      appendTagString(sb, tags)

      client.send(sb.toString)
    }
  }

  private def escapeEventString(title: String): String = title.replace("\n", "\\n")

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
  def recordSetValue(aspect: String, value: String, tags: String*): Unit = {
    // Documentation is light, but looking at dogstatsd source, we can send string values
    // here instead of numbers
    val sb = new JStringBuilder()

    sb.append(prefix)
    sb.append(aspect)
    sb.append(":")
    sb.append(value)
    sb.append("|s")
    appendTagString(sb, tags)

    client.send(sb.toString)
  }

  def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: Seq[String], m: Metric[A])(value: A): Unit = {
    def fullAspectName(aspect: String) = if (prefix == null || prefix.isEmpty) aspect else prefix + "." + aspect

    val tags = extraTags ++ m.tags(value)

    m.values(value).foreach {
      case IntegralGauge(aspect, v) => gauge(fullAspectName(aspect), v, sampleRate, tags: _*)
      case FractionalGauge(aspect, v) => gauge(fullAspectName(aspect), v, sampleRate, tags: _*)
      case IntegralHistogram(aspect, v) => histogram(fullAspectName(aspect), v, sampleRate, tags: _*)
      case FractionalHistogram(aspect, v) => histogram(fullAspectName(aspect), v, sampleRate, tags: _*)
      case Counter(aspect, v) => count(fullAspectName(aspect), v, sampleRate, tags: _*)
      case Timer(aspect, v) => time(fullAspectName(aspect), v.toMillis, sampleRate, tags: _*)
    }
  }

  private def validSample(sampleRate: SampleRate): Boolean = {
    !(sampleRate.value != 1 && Math.random > sampleRate.value)
  }
}
