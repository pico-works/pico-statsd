package org.pico.statsd

import java.net.{InetAddress, InetSocketAddress, UnknownHostException}
import java.util.concurrent.Callable

import com.timgroup.statsd.StatsDClientException

object Inet {
  /**
    * Create dynamic lookup for the given host name and port.
    *
    * @param hostname the host name of the targeted StatsD server
    * @param port     the port of the targeted StatsD server
    * @return a function to perform the lookup
    */
  def volatileAddressResolution(hostname: String, port: Int): () => InetSocketAddress = { () =>
    new InetSocketAddress(InetAddress.getByName(hostname), port)
  }

  /**
    * Lookup the address for the given host name and cache the result.
    *
    * @param hostname the host name of the targeted StatsD server
    * @param port     the port of the targeted StatsD server
    * @return a function that cached the result of the lookup
    * @throws Exception if the lookup fails, i.e. { @link UnknownHostException}
    */
  @throws[Exception]
  def staticAddressResolution(hostname: String, port: Int): () => InetSocketAddress = {
    val address: InetSocketAddress = volatileAddressResolution(hostname, port)()
    () => address
  }

  @throws[StatsDClientException]
  def staticStatsDAddressResolution(hostname: String, port: Int): () => InetSocketAddress = {
    try {
      staticAddressResolution(hostname, port)
    } catch {
      case e: Exception => throw new StatsDClientException("Failed to lookup StatsD host", e)
    }
  }
}
