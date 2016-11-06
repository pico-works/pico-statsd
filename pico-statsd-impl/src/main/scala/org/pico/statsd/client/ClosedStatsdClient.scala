package org.pico.statsd.client

import org.pico.event.{ClosedSource, Source}

class ClosedStatsdClient extends StatsdClient {
  override def errors: Source[Exception] = ClosedSource

  override def send[A: Metric](message: A): Unit = ()

  override def prefix: String = ""

  override def tagString(tags: Seq[String]): String = ""

  override def close(): Unit = ()
}
