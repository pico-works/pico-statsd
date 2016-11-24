package org.pico.statsd

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.lang.{StringBuilder => JStringBuilder}
import java.net.InetSocketAddress
import java.util.concurrent.Callable

import com.timgroup.statsd._
import org.pico.statsd.datapoint.{DataPointWritable, Sampler}

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
    var constantTags: Array[String] = Array.empty,
    val errorHandler: StatsDClientErrorHandler,
    val addressLookup: Callable[InetSocketAddress]) extends StatsdClient {
  override def sampleRate: SampleRate = SampleRate.always

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
  override def close(): Unit = client.close()

  val baos = new ByteArrayOutputStream(1000)
  val out = new PrintWriter(baos)

  override def send[D: DataPointWritable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    out.flush()
    baos.reset()

    // TODO: Write more tags
    DataPointWritable.of[D].write(out, prefix, aspect, sampleRate, d) { tagWriter =>
      constantTags.foreach(tagWriter.writeTag)
      tags.foreach(tagWriter.writeTag)
    }

    out.flush()

    client.send(ByteArrayWindow(baos.toByteArray, 0, baos.size()))
  }

  override def sample[S: Sampler](s: S): Unit = {
    Sampler.of[S].sendIn(this, s)
  }

  override def sampledAt(sampleRate: SampleRate): StatsdClient = new SamplingStatsdClient(this, sampleRate)
}
