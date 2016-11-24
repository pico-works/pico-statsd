package org.pico.statsd.impl

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, Sink, SinkSource}
import org.pico.statsd.StatsdClientException

case class UdpEmitFailed(
    address: InetSocketAddress,
    buffer: ByteBuffer,
    sentBytes: Int)

object UdpEmitter {
  def apply(addressLookup: () => InetSocketAddress): SinkSource[ByteBuffer, UdpEmitFailed] = {
    val clientChannel: DatagramChannel = try {
      DatagramChannel.open
    } catch {
      case e: Exception => throw StatsdClientException("Failed to start StatsD client", e)
    }

    val errors = Bus[UdpEmitFailed]

    val sink = Sink[ByteBuffer] { buffer =>
      val address = addressLookup()
      val sentBytes = clientChannel.send(buffer, address)

      if (buffer.limit() != sentBytes) {
        errors.publish(UdpEmitFailed(address, buffer, sentBytes))
      }
    }

    sink.disposes(clientChannel)

    SinkSource.from[ByteBuffer, UdpEmitFailed](sink, errors)
  }
}
