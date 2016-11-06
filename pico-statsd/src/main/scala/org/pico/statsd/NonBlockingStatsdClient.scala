package org.pico.statsd

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
    var prefix: String = null,
    val queueSize: Int,
    var constantTags: Array[String] = null,
    val addressLookup: () => InetSocketAddress) extends StatsdClient with SimpleDisposer {
  val errors = Bus[Exception]

  this.prefix = if (prefix != null && !prefix.isEmpty) {
    String.format("%s.", prefix)
  } else {
    ""
  }

  if (constantTags != null && constantTags.isEmpty) {
    constantTags = null
  }

  private val constantTagsRendered = if (constantTags != null) {
    NonBlockingStatsdClient.tagString(constantTags, null)
  } else {
    null
  }

  private val clientChannel = this.disposesOrClose {
    try {
      DatagramChannel.open
    } catch {
      case e: Exception => throw new StatsdException("Failed to start StatsD client", e)
    }
  }

  private val queue: BlockingQueue[String] = new LinkedBlockingQueue[String](queueSize)

  private val executor: ExecutorService = this.disposesOrClose(Executors.newSingleThreadExecutor(new ThreadFactory {
    val delegate: ThreadFactory = Executors.defaultThreadFactory

    override def newThread(r: Runnable): Thread = {
      val result: Thread = delegate.newThread(r)
      result.setName("StatsD-" + result.getName)
      result.setDaemon(true)
      result
    }
  }))

  executor.submit(new QueueConsumer(addressLookup))

  override def tagString(tags: String*): String = NonBlockingStatsdClient.tagString(tags.toArray, constantTagsRendered)

  override def send(message: String): Unit = queue.offer(message)

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
      val sizeOfBuffer: Int = sendBuffer.position
      sendBuffer.flip
      val sentBytes: Int = clientChannel.send(sendBuffer, address)
      sendBuffer.limit(sendBuffer.capacity)
      sendBuffer.rewind
      if (sizeOfBuffer != sentBytes) {
        errors.publish(new IOException(
          s"Could not send entirely stat $sendBuffer to host ${address.getHostName}:${address.getPort}. Only sent $sentBytes bytes out of $sizeOfBuffer bytes"))
      }
    }
  }
}


object NonBlockingStatsdClient {
  private val PACKET_SIZE_BYTES: Int = 1500

  def tagString(tags: Array[String], tagPrefix: String): String = {
    def appendTags(sb: StringBuilder): Unit = {
      var n = tags.length - 1

      while (n >= 0) {
        sb.append(tags(n))

        if (n > 0) {
          sb.append(",")
        }

        n -= 1
      }
    }

    if (tagPrefix != null) {
      if (tags == null || tags.length == 0) {
        tagPrefix
      } else {
        val sb = new StringBuilder(tagPrefix)
        sb.append(",")
        appendTags(sb)
        sb.toString
      }
    } else {
      if (tags == null || tags.length == 0) {
        ""
      } else {
        val sb = new StringBuilder("|#")
        appendTags(sb)
        sb.toString
      }
    }
  }

  val MESSAGE_CHARSET: Charset = Charset.forName("UTF-8")
}