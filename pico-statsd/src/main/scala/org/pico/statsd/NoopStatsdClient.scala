package org.pico.statsd
import org.pico.statsd.datapoint.{DataPointWritable, Sampler}

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def stop(): Unit = ()

  override def send[D: DataPointWritable](aspect: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = ()

  override def sample[S: Sampler](s: S): Unit = ()

  override def sampleRate: SampleRate = SampleRate.never

  override def sampledAt(sampleRate: SampleRate): StatsdClient = this
}
