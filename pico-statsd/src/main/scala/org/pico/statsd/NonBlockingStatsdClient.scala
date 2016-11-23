package org.pico.statsd

import com.timgroup.statsd._
import java.net.InetSocketAddress
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Callable

/**
  * A simple StatsD client implementation facilitating metrics recording.
  *
  * <p>Upon instantiation, this client will establish a socket connection to a StatsD instance
  * running on the specified host and port. Metrics are then sent over this connection as they are
  * received by the client.
  * </p>
  *
  * <p>Three key methods are provided for the submission of data-points for the application under
  * scrutiny:
  * <ul>
  * <li>{@link #incrementCounter} - adds one to the value of the specified named counter</li>
  * <li>{@link #recordGaugeValue} - records the latest fixed value for the specified named gauge</li>
  * <li>{@link #recordExecutionTime} - records an execution time in milliseconds for the specified named operation</li>
  * <li>{@link #recordHistogramValue} - records a value, to be tracked with average, maximum, and percentiles</li>
  * <li>{@link #recordEvent} - records an event</li>
  * <li>{@link #recordSetValue} - records a value in a set</li>
  * </ul>
  * From the perspective of the application, these methods are non-blocking, with the resulting
  * IO operations being carried out in a separate thread. Furthermore, these methods are guaranteed
  * not to throw an exception which may disrupt application execution.
  *
  * <p>As part of a clean system shutdown, the {@link #stop()} method should be invoked
  * on any StatsD clients.</p>
  *
  * @author Tom Denley, John Ky
  *
  */
object NonBlockingStatsdClient {
  /**
    * Because NumberFormat is not thread-safe we cannot share instances across threads. Use a ThreadLocal to
    * create one pre thread as this seems to offer a significant performance improvement over creating one per-thread:
    * http://stackoverflow.com/a/1285297/2648
    * https://github.com/indeedeng/java-dogstatsd-client/issues/4
    */
  private val NUMBER_FORMATTERS: ThreadLocal[NumberFormat] = new ThreadLocal[NumberFormat]() {
    override protected def initialValue: NumberFormat = {
      // Always create the formatter for the US locale in order to avoid this bug:
      // https://github.com/indeedeng/java-dogstatsd-client/issues/3
      val numberFormatter: NumberFormat = NumberFormat.getInstance(Locale.US)
      numberFormatter.setGroupingUsed(false)
      numberFormatter.setMaximumFractionDigits(6)
      // we need to specify a value for Double.NaN that is recognized by dogStatsD
      if (numberFormatter.isInstanceOf[DecimalFormat]) {
        // better safe than a runtime error
        val decimalFormat: DecimalFormat = numberFormatter.asInstanceOf[DecimalFormat]
        val symbols: DecimalFormatSymbols = decimalFormat.getDecimalFormatSymbols
        symbols.setNaN("NaN")
        decimalFormat.setDecimalFormatSymbols(symbols)
      }
      numberFormatter
    }
  }
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
    var prefix: String = null,
    val queueSize: Int,
    var constantTags: Array[String] = null,
    val errorHandler: StatsDClientErrorHandler,
    val addressLookup: Callable[InetSocketAddress]) extends StatsDClient {
  this.prefix = if ((prefix != null) && (!prefix.isEmpty)) {
    prefix + "."
  } else {
    ""
  }

  // Empty list should be null for faster comparison
  if ((constantTags != null) && constantTags.isEmpty) {
    constantTags = null
  }

  val constantTagsRendered = if (constantTags != null) {
    Tags.tagString(constantTags, null)
  } else {
    null
  }

  val client: InternalStatsdClient = new InternalStatsdClient(queueSize, errorHandler, addressLookup)

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
  def stop() {
    client.stop()
  }

  /**
    * Generate a suffix conveying the given tag list to the client
    */
  private def tagString(tags: Seq[String]): String = Tags.tagString(tags, constantTagsRendered)

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
    client.send(prefix + aspect + ":" + delta + "|c" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def count(aspect: String, delta: Long, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      client.send(prefix + aspect + ":" + delta + "|c|@" + sampleRate + tagString(tags))
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

  /**
    * {@inheritDoc }
    */
  override def incrementCounter(aspect: String, sampleRate: Double, tags: String*): Unit = {
    count(aspect, 1L, sampleRate, tags: _*)
  }

  /**
    * Convenience method equivalent to {@link #incrementCounter(String, String[])}.
    */
  override def increment(aspect: String, tags: String*): Unit = {
    incrementCounter(aspect, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def increment(aspect: String, sampleRate: Double, tags: String*): Unit = {
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

  /**
    * {@inheritDoc }
    */
  override def decrementCounter(aspect: String, sampleRate: Double, tags: String*): Unit = {
    count(aspect, -1, sampleRate, tags: _*)
  }

  /**
    * Convenience method equivalent to {@link #decrementCounter(String, String[])}.
    */
  override def decrement(aspect: String, tags: String*): Unit = {
    decrementCounter(aspect, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def decrement(aspect: String, sampleRate: Double, tags: String*): Unit = {
    decrementCounter(aspect, sampleRate, tags: _*)
  }

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param value
    * the new reading of the gauge
    * @param tags
    * array of tags to be added to the data
    */
  override def recordGaugeValue(aspect: String, value: Double, tags: String*): Unit = {
    /* Intentionally using %s rather than %f here to avoid
            * padding with extra 0s to represent precision */ client.send(prefix + aspect + ":" + StatsdNumberFormat.get.format(value) + "|g" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def recordGaugeValue(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      client.send(prefix + aspect + ":" + StatsdNumberFormat.get.format(value) + "|g|@" + sampleRate + tagString(tags))
    }
  }

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, double, String[])}.
    */
  override def gauge(aspect: String, value: Double, tags: String*): Unit = {
    recordGaugeValue(aspect, value, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def gauge(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = {
    recordGaugeValue(aspect, value, sampleRate, tags: _*)
  }

  /**
    * Records the latest fixed value for the specified named gauge.
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param aspect
    * the name of the gauge
    * @param value
    * the new reading of the gauge
    * @param tags
    * array of tags to be added to the data
    */
  override def recordGaugeValue(aspect: String, value: Long, tags: String*): Unit = {
    client.send(prefix + aspect + ":" + value + "|g" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def recordGaugeValue(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      client.send(prefix + aspect + ":" + value + "|g|@" + sampleRate + tagString(tags))
    }
  }

  /**
    * Convenience method equivalent to {@link #recordGaugeValue(String, long, String[])}.
    */
  override def gauge(aspect: String, value: Long, tags: String*): Unit = {
    recordGaugeValue(aspect, value, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def gauge(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = {
    recordGaugeValue(aspect, value, sampleRate, tags: _*)
  }

  /**
    * Records an execution time in milliseconds for the specified named operation.
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
  override def recordExecutionTime(aspect: String, timeInMs: Long, tags: String*): Unit = {
    client.send(prefix + aspect + ":" + timeInMs + "|ms" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def recordExecutionTime(aspect: String, timeInMs: Long, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      client.send(prefix + aspect + ":" + timeInMs + "|ms|@" + sampleRate + tagString(tags))
    }
  }

  /**
    * Convenience method equivalent to {@link #recordExecutionTime(String, long, String[])}.
    */
  override def time(aspect: String, value: Long, tags: String*): Unit = {
    recordExecutionTime(aspect, value, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def time(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = {
    recordExecutionTime(aspect, value, sampleRate, tags: _*)
  }

  /**
    * Records a value for the specified named histogram.
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
  override def recordHistogramValue(aspect: String, value: Double, tags: String*): Unit = {
    /* Intentionally using %s rather than %f here to avoid
            * padding with extra 0s to represent precision */ client.send(prefix + aspect + ":" + StatsdNumberFormat.get.format(value) + "|h" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def recordHistogramValue(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      // Intentionally using %s rather than %f here to avoid
      // padding with extra 0s to represent precision
      client.send(prefix + aspect + ":" + StatsdNumberFormat.get.format(value) + "|h|@" + sampleRate + tagString(tags))
    }
  }

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, double, String[])}.
    */
  override def histogram(aspect: String, value: Double, tags: String*): Unit = {
    recordHistogramValue(aspect, value, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def histogram(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = {
    recordHistogramValue(aspect, value, sampleRate, tags: _*)
  }

  /**
    * Records a value for the specified named histogram.
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
  override def recordHistogramValue(aspect: String, value: Long, tags: String*): Unit = {
    client.send(prefix + aspect + ":" + value + "|h" + tagString(tags))
  }

  /**
    * {@inheritDoc }
    */
  override def recordHistogramValue(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = {
    if (validSample(sampleRate)) {
      client.send(prefix + aspect + ":" + value + "|h|@" + sampleRate + tagString(tags))
    }
  }

  /**
    * Convenience method equivalent to {@link #recordHistogramValue(String, long, String[])}.
    */
  override def histogram(aspect: String, value: Long, tags: String*): Unit = {
    recordHistogramValue(aspect, value, tags: _*)
  }

  /**
    * {@inheritDoc }
    */
  override def histogram(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = {
    recordHistogramValue(aspect, value, sampleRate, tags: _*)
  }

  private def eventMap(event: Event): String = {
    val res: StringBuilder = new StringBuilder("")
    val millisSinceEpoch: Long = event.getMillisSinceEpoch
    if (millisSinceEpoch != -1) res.append("|d:").append(millisSinceEpoch / 1000)
    val hostname: String = event.getHostname
    if (hostname != null) res.append("|h:").append(hostname)
    val aggregationKey: String = event.getAggregationKey
    if (aggregationKey != null) res.append("|k:").append(aggregationKey)
    val priority: String = event.getPriority
    if (priority != null) res.append("|p:").append(priority)
    val alertType: String = event.getAlertType
    if (alertType != null) res.append("|t:").append(alertType)
    res.toString
  }

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
  override def recordEvent(event: Event, tags: String*): Unit = {
    val title: String = escapeEventString(prefix + event.getTitle)
    val text: String = escapeEventString(event.getText)
    client.send("_e{" + title.length + "," + text.length + "}:" + title + "|" + text + eventMap(event) + tagString(tags))
  }

  private def escapeEventString(title: String): String = title.replace("\n", "\\n")

  /**
    * Records a run status for the specified named service check.
    *
    * <p>This method is a DataDog extension, and may not work with other servers.</p>
    *
    * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
    *
    * @param sc
    * the service check object
    */
  override def recordServiceCheckRun(sc: ServiceCheck): Unit = client.send(toStatsDString(sc))

  private def toStatsDString(sc: ServiceCheck): String = {
    // see http://docs.datadoghq.com/guides/dogstatsd/#service-checks
    val sb: StringBuilder = new StringBuilder
    sb.append("_sc|")
    sb.append(sc.getName)
    sb.append("|")
    sb.append(sc.getStatus)
    if (sc.getTimestamp > 0) {
      sb.append("|d:")
      sb.append(sc.getTimestamp)
    }
    if (sc.getHostname != null) {
      sb.append("|h:")
      sb.append(sc.getHostname)
    }
    sb.append(tagString(sc.getTags))
    if (sc.getMessage != null) {
      sb.append("|m:")
      sb.append(sc.getEscapedMessage)
    }
    sb.toString
  }

  /**
    * Convenience method equivalent to {@link #recordServiceCheckRun(ServiceCheck sc)}.
    */
  override def serviceCheck(sc: ServiceCheck): Unit =recordServiceCheckRun(sc)

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
    client.send(prefix + aspect + ":" + value + "|s" + tagString(tags))
  }

  private def validSample(sampleRate: Double): Boolean = {
    !(sampleRate != 1 && Math.random > sampleRate)
  }
}
