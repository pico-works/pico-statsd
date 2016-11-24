package org.pico.statsd.impl

import java.net.{InetAddress, InetSocketAddress}

import org.pico.statsd.StatsdClientException

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

  def staticStatsdAddressResolution(hostname: String, port: Int): () => InetSocketAddress = {
    try {
      staticAddressResolution(hostname, port)
    } catch {
      case e: Exception => throw new StatsdClientException("Failed to lookup StatsD host", e)
    }
  }
}
