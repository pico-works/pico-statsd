package org.pico.statsd
import org.pico.statsd.datapoint.{DataPoints, Sampling}

object NoopStatsdClient extends StatsdClient {
  /**
    * Cleanly shut down this StatsD client. This method may throw an exception if
    * the socket cannot be closed.
    */
  override def stop(): Unit = ()

  override def sendMetrics[A](prefix: String, sampleRate: SampleRate, extraTags: Seq[String], m: Metric[A])(value: A): Unit = ()

  override def send[D: DataPoints : Sampling](d: D): Unit = ()
}
