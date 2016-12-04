package org.pico.statsd

import java.io.{Closeable, PrintWriter}
import java.nio.ByteBuffer
import java.util.concurrent._

import org.pico.event.{Bus, Source}
import org.pico.statsd.impl.AccessibleByteArrayOutputStream

import scala.util.control.NonFatal

final class InternalStatsdClient(val queueSize: Int) extends Closeable {
  private val _errors = Bus[Throwable]
  val errors: Source[Throwable] = _errors

  private val _messages = Bus[ByteBuffer]
  val messages: Source[ByteBuffer] = _messages

  private val queue: BlockingQueue[PrintWriter => Unit] = new LinkedBlockingQueue[PrintWriter => Unit](queueSize)

  private val executor: ExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory {
    def newThread(r: Runnable): Thread = {
      val thread = Executors.defaultThreadFactory.newThread(r)
      thread.setName("Statsd-" + thread.getName)
      thread.setDaemon(true)
      thread
    }
  })

  executor.submit(new QueueConsumer())

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

  def send(f: PrintWriter => Unit): Unit = queue.put(f)

  private class QueueConsumer extends Runnable {
    override def run(): Unit = {
      val baos = new AccessibleByteArrayOutputStream(InternalStatsdClient.packetSizeBytes * 2)
      val out = new PrintWriter(baos, true)

      try {
        while (!executor.isShutdown) {
          try {
            val printTo = queue.poll(1, TimeUnit.SECONDS)

            if (null != printTo) {
              val lastOffset = baos.size()

              if (lastOffset > 0) {
                out.print('\n')
                out.flush()
              }

              val lineOffset = baos.size

              printTo(out)
              out.flush()

              val nextOffset = baos.size

              if (nextOffset > InternalStatsdClient.packetSizeBytes) {
                _messages.publish(ByteBuffer.wrap(baos.byteArray, 0, lastOffset))
                baos.drop(lineOffset)
              } else if (queue.peek == null) {
                _messages.publish(ByteBuffer.wrap(baos.byteArray, 0, nextOffset))
                baos.drop(nextOffset)
              }
            }
          } catch {
            case NonFatal(e) =>
              _errors.publish(e)
          }
        }
      } catch {
        case e: Throwable => _errors.publish(e)
      }
    }
  }
}

object InternalStatsdClient {
  val packetSizeBytes: Int = 1400
}
