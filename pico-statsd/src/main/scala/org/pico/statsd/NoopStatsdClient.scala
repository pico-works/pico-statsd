package org.pico.statsd

import java.nio.ByteBuffer

import org.pico.event.{ClosedSource, Source}
import org.pico.statsd.datapoint.Sampler
import org.pico.statsd.impl.Printable

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = ()

  override def send[D: Printable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = ()

  override def sample[S: Sampler](s: S): Unit = ()

  override def sampleRate: SampleRate = SampleRate.never

  override def sampledAt(sampleRate: SampleRate): StatsdClient = this

  override def messages: Source[ByteBuffer] = ClosedSource
}
