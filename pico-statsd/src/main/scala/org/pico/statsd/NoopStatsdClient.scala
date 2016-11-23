package org.pico.statsd
import org.pico.statsd.datapoint.{DataPointWritable, Sampler, Sampling}

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def stop(): Unit = ()

  override def send[D: DataPointWritable](aspect: String, d: D): Unit = ()

  override def sample[S: Sampler: Sampling](s: S): Unit = ()
}
