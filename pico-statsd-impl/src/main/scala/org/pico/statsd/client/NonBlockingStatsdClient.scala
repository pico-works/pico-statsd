package org.pico.statsd.client

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.util.concurrent._

import org.pico.disposal.SimpleDisposer
import org.pico.disposal.std.autoCloseable._
import org.pico.disposal.std.executorService._
import org.pico.event.Bus

final class NonBlockingStatsdClient(
    val prefix: String,
    val queueSize: Int,
    val constantTags: Array[String] = Array.empty,
    val addressLookup: () => InetSocketAddress) extends StatsdClient with SimpleDisposer {
  val messagePrefix = if (!prefix.isEmpty) String.format("%s.", prefix) else ""

  val errors = Bus[Exception]

  private val constantTagsRendered = NonBlockingStatsdClient.tagString("", constantTags)
  private val clientChannel = this.disposesOrClose(DatagramChannel.open)
  private val queue: BlockingQueue[String] = new LinkedBlockingQueue[String](queueSize)
  private val executor: ExecutorService = this.disposesOrClose(Executors.newSingleThreadExecutor(StatsdDaemonThreadFactory))

  executor.submit(new QueueConsumer(addressLookup))

  override def tagString(tags: Seq[String]): String = NonBlockingStatsdClient.tagString(constantTagsRendered, tags.toArray)

  override def send[A: Metric](metric: A): Unit = queue.offer(Metric.of[A].encodeMetric(metric, messagePrefix, tagString))

  private class QueueConsumer(
      val addressLookup: () => InetSocketAddress) extends Runnable {
    final private val sendBuffer: ByteBuffer = ByteBuffer.allocate(NonBlockingStatsdClient.PACKET_SIZE_BYTES)

    override def run(): Unit = {
      while (!executor.isShutdown) {
        try {
          val message = queue.poll(1, TimeUnit.SECONDS)

          if (null != message) {
            val address = addressLookup()
            val data = message.getBytes(NonBlockingStatsdClient.MESSAGE_CHARSET)

            if (sendBuffer.remaining < (data.length + 1)) {
              blockingSend(address)
            }

            if (sendBuffer.position > 0) {
              sendBuffer.put('\n'.toByte)
            }

            sendBuffer.put(data)

            if (null == queue.peek) {
              blockingSend(address)
            }
          }
        } catch {
          case e: Exception => errors.publish(e)
        }
      }
    }

    private def blockingSend(address: InetSocketAddress): Unit = {
      val sizeOfBuffer = sendBuffer.position
      sendBuffer.flip

      val sentBytes = try {
        clientChannel.send(sendBuffer, address)
      } finally {
        sendBuffer.limit(sendBuffer.capacity)
        sendBuffer.rewind
      }

      if (sizeOfBuffer != sentBytes) {
        errors.publish(new IOException(
          s"Could not send entire stat $sendBuffer to host ${address.getHostName}:${address.getPort}. Only sent $sentBytes bytes out of $sizeOfBuffer bytes"))
      }
    }
  }
}

object NonBlockingStatsdClient {
  private val PACKET_SIZE_BYTES: Int = 1500

  def tagString(tagPrefix: String, tags: Array[String]): String = {
    if (tags.isEmpty) {
      tagPrefix
    } else {
      val sb = if (tagPrefix.nonEmpty) new StringBuilder(tagPrefix).append(",") else new StringBuilder("|#")
      sb.append(sb.append(tags.mkString(",")))
      sb.toString
    }
  }

  val MESSAGE_CHARSET: Charset = Charset.forName("UTF-8")
}
