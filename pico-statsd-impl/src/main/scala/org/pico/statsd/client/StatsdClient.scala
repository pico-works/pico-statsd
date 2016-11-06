package org.pico.statsd.client

import java.io.Closeable

import org.pico.event.Source
import org.pico.statsd.client.metrics.Event

trait StatsdClient extends Closeable {
  def errors: Source[Exception]

  def send[A: Metric](metric: A): Unit

  def prefix: String

  def tagString(tags: Seq[String]): String
}

object StatsdClient {
  def escapeEventString(title: String): String = title.replace("\n", "\\n")

  def eventMap(event: Event): String = {
    val res = new StringBuilder("")

    if (event.millisSinceEpoch != -1) {
      res.append("|d:").append(event.millisSinceEpoch / 1000)
    }

    if (event.hostname.nonEmpty) {
      res.append("|h:").append(event.hostname)
    }

    if (event.aggregationKey != null) {
      res.append("|k:").append(event.aggregationKey)
    }

    if (event.priority.nonEmpty) {
      res.append("|p:").append(event.priority)
    }

    if (event.alertType.nonEmpty) {
      res.append("|t:").append(event.alertType)
    }

    res.toString
  }
}
