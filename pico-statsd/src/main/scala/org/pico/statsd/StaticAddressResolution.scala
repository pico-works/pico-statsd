package org.pico.statsd

import java.net.{InetAddress, InetSocketAddress}

object StaticAddressResolution {
  /**
    * Lookup the address for the given host name and cache the result.
    *
    * @param hostname the host name of the targeted StatsD server
    * @param port     the port of the targeted StatsD server
    * @return a function that cached the result of the lookup
    * @throws Exception if the lookup fails, i.e. { @link UnknownHostException}
    */
  def apply(hostname: String, port: Int): () => InetSocketAddress = {
    val address = new InetSocketAddress(InetAddress.getByName(hostname), port)
    () => address
  }
}
