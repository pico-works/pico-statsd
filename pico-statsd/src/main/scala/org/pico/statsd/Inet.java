package org.pico.statsd;

import com.timgroup.statsd.StatsDClientException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class Inet {
    /**
     * Create dynamic lookup for the given host name and port.
     *
     * @param hostname the host name of the targeted StatsD server
     * @param port     the port of the targeted StatsD server
     * @return a function to perform the lookup
     */
    public static Callable<InetSocketAddress> volatileAddressResolution(final String hostname, final int port) {
        return new Callable<InetSocketAddress>() {
            @Override public InetSocketAddress call() throws UnknownHostException {
                return new InetSocketAddress(InetAddress.getByName(hostname), port);
            }
        };
    }

    /**
     * Lookup the address for the given host name and cache the result.
     *
     * @param hostname the host name of the targeted StatsD server
     * @param port     the port of the targeted StatsD server
     * @return a function that cached the result of the lookup
     * @throws Exception if the lookup fails, i.e. {@link UnknownHostException}
     */
    public static Callable<InetSocketAddress> staticAddressResolution(final String hostname, final int port) throws Exception {
        final InetSocketAddress address = volatileAddressResolution(hostname, port).call();
        return new Callable<InetSocketAddress>() {
            @Override public InetSocketAddress call() {
                return address;
            }
        };
    }

    public static Callable<InetSocketAddress> staticStatsDAddressResolution(final String hostname, final int port) throws StatsDClientException {
        try {
            return staticAddressResolution(hostname, port);
        } catch (final Exception e) {
            throw new StatsDClientException("Failed to lookup StatsD host", e);
        }
    }
}
