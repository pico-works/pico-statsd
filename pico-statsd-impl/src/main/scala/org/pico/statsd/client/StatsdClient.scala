package org.pico.statsd.client

import java.io.Closeable

import org.pico.event.Source
import org.pico.statsd.client.metrics.Event
import org.pico.statsd.syntax.show._

trait StatsdClient extends Closeable {
  def errors: Source[Exception]

  def send[A: Metric](metric: A): Unit

  def prefix: String

  def tagString(tags: Seq[String]): String
}

object StatsdClient {
  def escapeEventString(title: String): String = title.replace("\n", "\\n")

  def eventMap(event: Event): String = {
    val res: StringBuilder = new StringBuilder("")
    val millisSinceEpoch: Long = event.millisSinceEpoch

    if (millisSinceEpoch != -1) {
      res.append("|d:").append(millisSinceEpoch / 1000)
    }

    val hostname: String = event.hostname

    if (hostname != null) {
      res.append("|h:").append(hostname)
    }

    val aggregationKey: String = event.aggregationKey

    if (aggregationKey != null) {
      res.append("|k:").append(aggregationKey)
    }

    val priority: String = event.priority

    if (priority != null) {
      res.append("|p:").append(priority)
    }

    val alertType: String = event.alertType

    if (alertType != null) {
      res.append("|t:").append(alertType)
    }

    res.toString
  }
}
