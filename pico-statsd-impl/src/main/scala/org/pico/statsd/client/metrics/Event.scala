package org.pico.statsd.client.metrics

import org.pico.statsd.client.{Metric, StatsdClient}
import org.pico.statsd.syntax.show._

case class Event(
    title: String,
    text: String,
    millisSinceEpoch: Long,
    hostname: String,
    aggregationKey: String,
    priority: String ,
    sourceTypeName: String,
    alertType: String,
    tags: Seq[String])

object Event {
  implicit val metric_Event_d4RaaNY = new Metric[Event] {
    override def encodeMetric(metric: Event, prefix: String, tagger: Seq[String] => String): String = {
      val title: String = StatsdClient.escapeEventString(prefix + metric.title)
      val text: String = StatsdClient.escapeEventString(metric.text)
      show"_e{${title.length},${text.length}:$title|$text${StatsdClient.eventMap(metric)}${tagger(metric.tags)}"
    }
  }
}