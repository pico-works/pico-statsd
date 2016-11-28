package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.Source
import org.pico.statsd.datapoint.Metric
import org.pico.statsd.impl.Printable

class ConfiguredStatsdClient(
    impl: StatsdClient,
    override val aspect: String,
    override val sampleRate: SampleRate) extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = impl.close()

  override def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    impl.send(aspect, metric, sampleRate, d, tags)
  }

  override def sample[S: Metric](s: S): Unit = {
    if (validSample(sampleRate)) {
      Metric.of[S].sendIn(this, s)
    }
  }

  override def sampledAt(sampleRate: SampleRate): StatsdClient = {
    new ConfiguredStatsdClient(impl, aspect, sampleRate)
  }

  override def withAspect(aspect: String): StatsdClient = {
    new ConfiguredStatsdClient(impl, aspect, sampleRate)
  }

  override def messages: Source[ByteBuffer] = impl.messages
}
