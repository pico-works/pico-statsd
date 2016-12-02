package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.Source
import org.pico.statsd.datapoint.Metric
import org.pico.statsd.impl.Printable

case class ConfiguredStatsdClient(
    impl: StatsdClient,
    config: StatsdConfig) extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = impl.close()

  override def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    impl.send(aspect, metric, sampleRate, d, tags)
  }

  override def messages: Source[ByteBuffer] = impl.messages

  override def configured(config: StatsdConfig): StatsdClient = new ConfiguredStatsdClient(impl, config)
}
