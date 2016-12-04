package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.{ClosedSource, Source}

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = ()

  override def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = ()

  override def messages: Source[ByteBuffer] = ClosedSource

  override def config: StatsdConfig = StatsdConfig("", SampleRate.never)

  override def configured(config: StatsdConfig): StatsdClient = this
}
