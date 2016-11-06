package org.pico.statsd.client.metrics

import org.pico.statsd.client.{Metric, NumberFormatters, Status}
import org.pico.statsd.syntax.show._

case class ServiceCheck(
    name: String,
    hostname: String,
    message: String,
    checkRunId: Int,
    timestamp: Int,
    status: Status,
    tags: Array[String]) {
  def escapedMessage: String = message.replace("\n", "\\n").replace("m:", "m\\:")
}

object ServiceCheck {
  implicit val serviceCheck_Metric_3qR2szH = new Metric[ServiceCheck] {
    override def encodeMetric(metric: ServiceCheck, prefix: String, tagger: Seq[String] => String): String = {
      val sb: StringBuilder = new StringBuilder
      sb.append(show"_sc|${metric.name}|${metric.status.value}")
      if (metric.timestamp > 0) sb.append(show"|d:${metric.timestamp}")
      if (metric.hostname != null) sb.append(String.format(show"|h:${metric.hostname}"))
      sb.append(tagger(metric.tags))
      if (metric.message != null) sb.append(String.format(show"|m:${metric.escapedMessage}"))
      sb.toString
    }
  }
}
