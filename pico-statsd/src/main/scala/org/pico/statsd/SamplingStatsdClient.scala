package org.pico.statsd

import org.pico.statsd.datapoint.Sampler
import org.pico.statsd.impl.Printable

class SamplingStatsdClient(impl: StatsdClient, override val sampleRate: SampleRate) extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def close(): Unit = impl.close()

  override def send[D: Printable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    impl.send(aspect, sampleRate, d, tags)
  }

  override def sample[S: Sampler](s: S): Unit = {
    if (validSample(sampleRate)) {
      Sampler.of[S].sendIn(this, s)
    }
  }

  override def sampledAt(sampleRate: SampleRate): StatsdClient = new SamplingStatsdClient(impl, sampleRate)
}
