package org.pico.statsd

import com.timgroup.statsd.StatsDClientErrorHandler
import com.timgroup.statsd.StatsDClientException
import java.io.{Closeable, IOException}
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent._

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
  * @param errorHandler  handler to use when an exception occurs during usage, may be null to indicate noop
  * @param addressLookup yields the IP address and socket of the StatsD server
  * @param queueSize     the maximum amount of unprocessed messages in the BlockingQueue.
  * @throws StatsDClientException if the client could not be started
  */
final class InternalStatsdClient(
    val queueSize: Int,
    val errorHandler: StatsDClientErrorHandler,
    val addressLookup: Callable[InetSocketAddress]) extends Closeable {
  if (errorHandler == null) handler = InternalStatsdClient.NO_OP_HANDLER
  else handler = errorHandler
  try
    clientChannel = DatagramChannel.open

  catch {
    case e: Exception => {
      throw new StatsDClientException("Failed to start StatsD client", e)
    }
  }
  queue = new LinkedBlockingQueue[ByteArrayWindow](queueSize)
  executor.submit(new InternalStatsdClient#QueueConsumer(addressLookup))
  final private var clientChannel: DatagramChannel = null
  final private var handler: StatsDClientErrorHandler = null
  final private val executor: ExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
    final private[statsd] val delegate: ThreadFactory = Executors.defaultThreadFactory
    def newThread(r: Runnable): Thread
    =
    {
      val result: Thread = delegate.newThread(r)
      result.setName("StatsD-" + result.getName)
      result.setDaemon(true)
      result
    }
  })
  final private var queue: BlockingQueue[ByteArrayWindow] = null

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
    * @param errorHandler handler to use when an exception occurs during usage, may be null to indicate noop
    * @param queueSize    the maximum amount of unprocessed messages in the BlockingQueue.
    * @throws StatsDClientException if the client could not be started
    */
  def this(hostname: String, port: Int, queueSize: Int, errorHandler: StatsDClientErrorHandler) {
    this(queueSize, errorHandler, Inet.staticStatsDAddressResolution(hostname, port))
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
      case e: Exception => handler.handle(e)
    } finally {
      if (clientChannel != null) {
        try {
          clientChannel.close()
        } catch {
          case e: IOException => handler.handle(e)
        }
      }
    }
  }

  def send(message: ByteArrayWindow) {
    queue.offer(message)
  }

  private class QueueConsumer private[statsd](val addressLookup: Callable[InetSocketAddress]) extends Runnable {
    final private val sendBuffer: ByteBuffer = ByteBuffer.allocate(InternalStatsdClient.PACKET_SIZE_BYTES)

    def run() {
      while (!executor.isShutdown) try
        val message: ByteArrayWindow = queue.poll(1, TimeUnit.SECONDS)
        if (null != message) {
          val address: InetSocketAddress = addressLookup.call
          if (sendBuffer.remaining < (message.length + 1)) blockingSend(address)
          if (sendBuffer.position > 0) sendBuffer.put('\n'.toByte)
          sendBuffer.put(message.array, message.start, message.length)
          if (null == queue.peek) blockingSend(address)
        }

      catch {
        case e: Exception => {
          handler.handle(e)
        }
      }
    }

    @throws[IOException]
    private def blockingSend(address: InetSocketAddress) {
      val sizeOfBuffer: Int = sendBuffer.position
      sendBuffer.flip
      val sentBytes: Int = clientChannel.send(sendBuffer, address)
      sendBuffer.limit(sendBuffer.capacity)
      sendBuffer.rewind
      if (sizeOfBuffer != sentBytes) handler.handle(new IOException(String.format("Could not send entirely stat %s to host %s:%d. Only sent %d bytes out of %d bytes", sendBuffer.toString, address.getHostName, address.getPort, sentBytes, sizeOfBuffer)))
    }
  }

}

object InternalStatsdClient {
  private val PACKET_SIZE_BYTES: Int = 1400
  private val NO_OP_HANDLER: StatsDClientErrorHandler = new StatsDClientErrorHandler() {
    def handle(e: Exception) {
      /* No-op */
    }
  }
  val MESSAGE_CHARSET: Charset = Charset.forName("UTF-8")
}
