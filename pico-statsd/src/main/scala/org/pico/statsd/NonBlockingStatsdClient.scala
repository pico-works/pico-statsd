package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.Source
import org.pico.statsd.datapoint.Sampler
import org.pico.statsd.impl.Printable

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
  * handler to use when an exception occurs during usage, may be null to indicate noop
  * @param queueSize
  * the maximum amount of unprocessed messages in the BlockingQueue.
  * @throws StatsdClientException
  * if the client could not be started
  */
final class NonBlockingStatsdClient(
    val prefix: String = "",
    val queueSize: Int,
    var constantTags: Array[String] = Array.empty) extends StatsdClient {
  override def sampleRate: SampleRate = SampleRate.always

  val client = new InternalStatsdClient(queueSize)

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
    * @throws StatsdClientException
    * if the client could not be started
    */
  def this(prefix: String) {
    this(prefix, Integer.MAX_VALUE)
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
    * @param constantTags
    * tags to be added to all content sent
    * @throws StatsdClientException
    * if the client could not be started
    */
  def this(prefix: String, constantTags: String*) {
    this(
      prefix,
      Integer.MAX_VALUE,
      constantTags.toArray)
  }

  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = client.close()

  override def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    client.send { out =>
      Printable.of[D].write(out, List(prefix, aspect).filter(_.nonEmpty).mkString("."), metric, sampleRate, d) { tagWriter =>
        constantTags.foreach(tagWriter.writeTag)
        tags.foreach(tagWriter.writeTag)
      }
    }
  }

  override def sample[S: Sampler](s: S): Unit = {
    Sampler.of[S].sendIn(this, s)
  }

  override def sampledAt(sampleRate: SampleRate): StatsdClient = {
    new ConfiguredStatsdClient(this, aspect, sampleRate)
  }

  override def withAspect(aspect: String): StatsdClient = {
    new ConfiguredStatsdClient(this, aspect, sampleRate)
  }

  override def messages: Source[ByteBuffer] = client.messages

  override def aspect: String = ""
}
