package org.pico.statsd

import com.timgroup.statsd.StatsDClientException
import java.io.{Closeable, IOException}
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent._

import org.pico.event.{Bus, Source}

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
  * @throws StatsDClientException if the client could not be started
  */
final class InternalStatsdClient(
    val queueSize: Int,
    val addressLookup: () => InetSocketAddress) extends Closeable {
  private val _errors = Bus[Exception]
  val errors: Source[Exception] = _errors

  private val clientChannel = try {
    DatagramChannel.open
  } catch {
    case e: Exception => throw new StatsDClientException("Failed to start StatsD client", e)
  }

  private val queue: BlockingQueue[ByteArrayWindow] = new LinkedBlockingQueue[ByteArrayWindow](queueSize)

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
    * @throws StatsDClientException if the client could not be started
    */
  def this(hostname: String, port: Int, queueSize: Int) {
    this(queueSize, Inet.staticStatsDAddressResolution(hostname, port))
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

  def send(message: ByteArrayWindow) {
    queue.offer(message)
  }

  private class QueueConsumer private[statsd](val addressLookup: () => InetSocketAddress) extends Runnable {
    final private val sendBuffer: ByteBuffer = ByteBuffer.allocate(InternalStatsdClient.PACKET_SIZE_BYTES)

    def run() {
      while (!executor.isShutdown) try {
        val message: ByteArrayWindow = queue.poll(1, TimeUnit.SECONDS)
        if (null != message) {
          val address: InetSocketAddress = addressLookup()
          if (sendBuffer.remaining < (message.length + 1)) blockingSend(address)
          if (sendBuffer.position > 0) sendBuffer.put('\n'.toByte)
          sendBuffer.put(message.array, message.start, message.length)
          if (null == queue.peek) blockingSend(address)
        }
      } catch {
        case e: Exception => _errors.publish(e)
      }
    }

    @throws[IOException]
    private def blockingSend(address: InetSocketAddress) {
      val sizeOfBuffer: Int = sendBuffer.position
      sendBuffer.flip
      val sentBytes: Int = clientChannel.send(sendBuffer, address)
      sendBuffer.limit(sendBuffer.capacity)
      sendBuffer.rewind
      if (sizeOfBuffer != sentBytes) {
        _errors.publish(new IOException(
          s"Could not send entirely stat $sendBuffer to host ${address.getHostName}:${address.getPort}. Only sent $sentBytes bytes out of $sizeOfBuffer bytes"))
      }
    }
  }

}

object InternalStatsdClient {
  private val PACKET_SIZE_BYTES: Int = 1400
  val MESSAGE_CHARSET: Charset = Charset.forName("UTF-8")
}
