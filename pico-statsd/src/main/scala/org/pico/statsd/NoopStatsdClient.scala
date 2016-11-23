package org.pico.statsd
import org.pico.statsd.datapoint.{DataPointWritable, Sampleable, Sampling}

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def stop(): Unit = ()

  override def send[D: DataPointWritable](d: D): Unit = ()

  override def sample[S: Sampleable: Sampling](s: S): Unit = ()
}
