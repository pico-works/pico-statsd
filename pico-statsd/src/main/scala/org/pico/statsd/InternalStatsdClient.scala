package org.pico.statsd

import java.io.{ByteArrayOutputStream, Closeable, IOException, PrintWriter}
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent._

import org.pico.event.{Bus, Source}
import org.pico.logging.Logger
import org.pico.statsd.impl.{AccessibleByteArrayOutputStream, ByteArrayWindow, Inet}

import scala.util.control.NonFatal

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
  * @param addressLookup yields the IP address and socket of the StatsD server
  * @param queueSize     the maximum amount of unprocessed messages in the BlockingQueue.
  * @throws StatsdClientException if the client could not be started
  */
final class InternalStatsdClient(
    val queueSize: Int,
    val addressLookup: () => InetSocketAddress) extends Closeable {
  val log = Logger[this.type]

  log.info("Creating internal statsd client")

  private val _errors = Bus[Throwable]
  val errors: Source[Throwable] = _errors

  private val clientChannel = try {
    DatagramChannel.open
  } catch {
    case e: Exception => throw StatsdClientException("Failed to start StatsD client", e)
  }

  private val queue: BlockingQueue[PrintWriter => Unit] = new LinkedBlockingQueue[PrintWriter => Unit](queueSize)

  private val executor: ExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory {
    final private[statsd] val delegate: ThreadFactory = Executors.defaultThreadFactory
    def newThread(r: Runnable): Thread = {
      val result: Thread = delegate.newThread(r)
      result.setName("StatsD-" + result.getName)
      result.setDaemon(true)
      result
    }
  })

  executor.submit(new QueueConsumer(addressLookup))

  log.info("Internal statsd client creation complete")

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
    * @param hostname     the host name of the targeted StatsD server
    * @param port         the port of the targeted StatsD server
    * @param queueSize    the maximum amount of unprocessed messages in the BlockingQueue.
    * @throws StatsdClientException if the client could not be started
    */
  def this(hostname: String, port: Int, queueSize: Int) {
    this(queueSize, Inet.staticStatsdAddressResolution(hostname, port))
  }

  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = {
    try {
      executor.shutdown()
      executor.awaitTermination(30, TimeUnit.SECONDS)
    } catch {
      case e: Exception => _errors.publish(e)
    } finally {
      if (clientChannel != null) {
        try {
          clientChannel.close()
        } catch {
          case e: IOException => _errors.publish(e)
        }
      }
    }
  }

  def send(f: PrintWriter => Unit) {
    queue.offer(f)
  }

  private class QueueConsumer private[statsd](val addressLookup: () => InetSocketAddress) extends Runnable {
    def run() {
      log.info("Statsd push thread started")

      val baos = new AccessibleByteArrayOutputStream(InternalStatsdClient.PACKET_SIZE_BYTES * 2)
      val out = new PrintWriter(baos, true)

      try {
        while (!executor.isShutdown) {
          try {
            val printTo = queue.poll(1, TimeUnit.SECONDS)

            if (null != printTo) {
              val address: InetSocketAddress = addressLookup()

              val lastOffset = baos.size()

              if (lastOffset > 0) {
                out.print('\n')
                out.flush()
              }

              val lineOffset = baos.size()

              printTo(out)
              out.flush()

              val nextOffset = baos.size

              if (nextOffset > InternalStatsdClient.PACKET_SIZE_BYTES) {
                blockingSend(address, baos.byteArray, 0, lastOffset)
                baos.drop(lineOffset)
              } else if (queue.peek == null) {
                blockingSend(address, baos.byteArray, 0, nextOffset)
                baos.drop(nextOffset)
              }
            }
          } catch {
            case NonFatal(e) =>
              log.warn("Error in statsd push thread", e)
              _errors.publish(e)
          }
        }
      } catch {
        case e: Throwable => log.warn("Fatal error in statsd push thread", e)
      }
    }

    @throws[IOException]
    private def blockingSend(address: InetSocketAddress, buffer: Array[Byte], start: Int, length: Int) {
      val sentBytes = clientChannel.send(ByteBuffer.wrap(buffer, start, length), address)

      if (length != sentBytes) {
        _errors.publish(new IOException(
          s"Could not send entirely stat '${new String(buffer, start, length)}' to host ${address.getHostName}:${address.getPort}. Only sent $sentBytes bytes out of $length bytes"))
      }
    }
  }
}

object InternalStatsdClient {
  private val PACKET_SIZE_BYTES: Int = 1400
  val MESSAGE_CHARSET: Charset = Charset.forName("UTF-8")
}
