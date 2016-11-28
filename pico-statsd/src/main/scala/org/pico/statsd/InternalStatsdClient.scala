package org.pico.statsd

import java.io.{Closeable, PrintWriter}
import java.nio.ByteBuffer
import java.util.concurrent._

import org.pico.event.{Bus, Source}
import org.pico.logging.Logger
import org.pico.statsd.impl.{ByteArrayPrintWriter, ByteArrayWindow}

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
  * @param queueSize     the maximum amount of unprocessed messages in the BlockingQueue.
  * @throws StatsdClientException if the client could not be started
  */
final class InternalStatsdClient(val queueSize: Int) extends Closeable {
  val log = Logger[this.type]

  log.info("Creating internal statsd client")

  private val _errors = Bus[Throwable]
  val errors: Source[Throwable] = _errors

  private val _messages = Bus[ByteBuffer]
  val messages: Source[ByteBuffer] = _messages

  private val queue: BlockingQueue[ByteArrayWindow] = new LinkedBlockingQueue[ByteArrayWindow](queueSize)

  private val executor: ExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory {
    def newThread(r: Runnable): Thread = {
      val thread = Executors.defaultThreadFactory.newThread(r)
      thread.setName("Statsd-" + thread.getName)
      thread.setDaemon(true)
      thread
    }
  })

  executor.submit(new QueueConsumer())

  log.info("Internal statsd client creation complete")

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
    }
  }

  def send(f: PrintWriter => Unit): Unit = {
    val out = ThreadLocalPrintWriter.get()

    f(out)

    out.flush()

    queue.put(out.outputStream.takeWindow())
  }

  object ThreadLocalPrintWriter extends ThreadLocal[ByteArrayPrintWriter] {
    override protected def initialValue: ByteArrayPrintWriter = {
      new ByteArrayPrintWriter(InternalStatsdClient.packetSizeBytes)
    }
  }

  private class QueueConsumer extends Runnable {
    override def run(): Unit = {
      log.info("Statsd push thread started")

      try {
        val buffer = new Array[Byte](InternalStatsdClient.packetSizeBytes)
        var offset = 0

        while (!executor.isShutdown) {
          try {
            val data = queue.poll(1, TimeUnit.SECONDS)

            if (null != data) {
              if (offset + data.length + 1 > InternalStatsdClient.packetSizeBytes) {
                _messages.publish(ByteBuffer.wrap(buffer, 0, offset))
                offset = 0
              } else if (queue.peek == null && offset > 0) {
                _messages.publish(ByteBuffer.wrap(buffer, 0, offset))
                offset = 0
              } else {
                if (offset > 0) {
                  buffer(offset) = '\n'.toByte
                  offset += 1
                }

                System.arraycopy(data.array, data.start, buffer, offset, data.length)
                offset += data.length
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

  }
}

object InternalStatsdClient {
  val packetSizeBytes: Int = 1400
}
