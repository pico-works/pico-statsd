package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.Source
import org.pico.statsd.impl.Printable

case class NonBlockingStatsdClient private (
    prefix: String = "",
    constantTags: Array[String],
    client: InternalStatsdClient) extends StatsdClient {
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

  override def messages: Source[ByteBuffer] = client.messages

  override def config: StatsdConfig = StatsdConfig.default

  override def configured(config: StatsdConfig): StatsdClient = ConfiguredStatsdClient(this, config)
}

object NonBlockingStatsdClient {
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
  def apply(
      prefix: String,
      queueSize: Int,
      constantTags: Array[String]): NonBlockingStatsdClient = {
    new NonBlockingStatsdClient(
      prefix,
      constantTags,
      new InternalStatsdClient(queueSize))
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
    * @throws StatsdClientException
    * if the client could not be started
    */
  def apply(prefix: String): NonBlockingStatsdClient = {
    NonBlockingStatsdClient(prefix, Integer.MAX_VALUE, Array.empty[String])
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
  def apply(prefix: String, constantTags: String*): NonBlockingStatsdClient = {
    NonBlockingStatsdClient(
      prefix,
      Integer.MAX_VALUE,
      constantTags.toArray)
  }
}
