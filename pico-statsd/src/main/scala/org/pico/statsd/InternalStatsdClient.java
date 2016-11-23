package org.pico.statsd;

import com.timgroup.statsd.StatsDClientErrorHandler;
import com.timgroup.statsd.StatsDClientException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.concurrent.*;

public final class InternalStatsdClient {
    private static final int PACKET_SIZE_BYTES = 1400;

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override
        public void handle(final Exception e) { /* No-op */ }
    };

    private final DatagramChannel clientChannel;
    private final StatsDClientErrorHandler handler;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        final ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(final Runnable r) {
            final Thread result = delegate.newThread(r);
            result.setName("StatsD-" + result.getName());
            result.setDaemon(true);
            return result;
        }
    });

    private final BlockingQueue<ByteArrayWindow> queue;

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
    public InternalStatsdClient(
            final String hostname,
            final int port,
            final int queueSize,
            final StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        this(queueSize, errorHandler, Inet.staticStatsDAddressResolution(hostname, port));
    }

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
    public InternalStatsdClient(
            final int queueSize,
            final StatsDClientErrorHandler errorHandler,
            final Callable<InetSocketAddress> addressLookup) throws StatsDClientException {
        if (errorHandler == null) {
            handler = NO_OP_HANDLER;
        } else {
            handler = errorHandler;
        }

        try {
            clientChannel = DatagramChannel.open();
        } catch (final Exception e) {
            throw new StatsDClientException("Failed to start StatsD client", e);
        }
        queue = new LinkedBlockingQueue<ByteArrayWindow>(queueSize);
        executor.submit(new QueueConsumer(addressLookup));
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (final Exception e) {
            handler.handle(e);
        } finally {
            if (clientChannel != null) {
                try {
                    clientChannel.close();
                } catch (final IOException e) {
                    handler.handle(e);
                }
            }
        }
    }

    public void send(final ByteArrayWindow message) {
        queue.offer(message);
    }

    public static final Charset MESSAGE_CHARSET = Charset.forName("UTF-8");

    private class QueueConsumer implements Runnable {
        private final ByteBuffer sendBuffer = ByteBuffer.allocate(PACKET_SIZE_BYTES);

        private final Callable<InetSocketAddress> addressLookup;

        QueueConsumer(final Callable<InetSocketAddress> addressLookup) {
            this.addressLookup = addressLookup;
        }

        @Override
        public void run() {
            while (!executor.isShutdown()) {
                try {
                    final ByteArrayWindow message = queue.poll(1, TimeUnit.SECONDS);
                    if (null != message) {
                        final InetSocketAddress address = addressLookup.call();

                        if (sendBuffer.remaining() < (message.length() + 1)) {
                            blockingSend(address);
                        }

                        if (sendBuffer.position() > 0) {
                            sendBuffer.put((byte) '\n');
                        }

                        sendBuffer.put(message.array(), message.start(), message.length());

                        if (null == queue.peek()) {
                            blockingSend(address);
                        }
                    }
                } catch (final Exception e) {
                    handler.handle(e);
                }
            }
        }

        private void blockingSend(final InetSocketAddress address) throws IOException {
            final int sizeOfBuffer = sendBuffer.position();
            sendBuffer.flip();

            final int sentBytes = clientChannel.send(sendBuffer, address);
            sendBuffer.limit(sendBuffer.capacity());
            sendBuffer.rewind();

            if (sizeOfBuffer != sentBytes) {
                handler.handle(
                        new IOException(
                                String.format(
                                        "Could not send entirely stat %s to host %s:%d. Only sent %d bytes out of %d bytes",
                                        sendBuffer.toString(),
                                        address.getHostName(),
                                        address.getPort(),
                                        sentBytes,
                                        sizeOfBuffer)));
            }
        }
    }
}
