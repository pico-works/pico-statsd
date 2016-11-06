package org.pico.statsd

import java.io.Closeable

import org.pico.event.Source
import org.pico.statsd.syntax.show._

trait StatsdClient extends Closeable {
  def errors: Source[Exception]

  def send(message: String): Unit

  def prefix: String

  def tagString(tags: String*): String

  def count(aspect: String, delta: Long, tags: String*): Unit = send(show"$prefix$aspect:$delta|c${tagString(tags: _*)}")

  def incrementCounter(aspect: String, tags: String*): Unit = count(aspect, 1L, tags: _*)

  def increment(aspect: String, tags: String*): Unit = incrementCounter(aspect, tags: _*)

  def decrementCounter(aspect: String, tags: String*): Unit = count(aspect, -1, tags: _*)

  def decrement(aspect: String, tags: String*): Unit = decrementCounter(aspect, tags: _*)

  def recordGaugeValue(aspect: String, value: Long, tags: String*): Unit = send(show"$prefix$aspect:$value|g${tagString(tags: _*)}")

  def gauge(aspect: String, value: Double, tags: String*): Unit = recordGaugeValue(aspect, value, tags: _*)

  def recordGaugeValue(aspect: String, value: Double, tags: String*): Unit = send(String.format("%s%s:%s|g%s", prefix, aspect, NumberFormatters.get.format(value), tagString(tags: _*)))

  def gauge(aspect: String, value: Long, tags: String*): Unit = recordGaugeValue(aspect, value, tags: _*)

  def recordExecutionTime(aspect: String, timeInMs: Long, tags: String*): Unit = send(show"$prefix$aspect:$timeInMs|ms${tagString(tags: _*)}")

  def time(aspect: String, value: Long, tags: String*): Unit = recordExecutionTime(aspect, value, tags: _*)

  def recordHistogramValue(aspect: String, value: Double, tags: String*): Unit = send(String.format("%s%s:%s|h%s", prefix, aspect, NumberFormatters.get.format(value), tagString(tags: _*)))

  def histogram(aspect: String, value: Double, tags: String*): Unit = recordHistogramValue(aspect, value, tags: _*)

  def recordHistogramValue(aspect: String, value: Long, tags: String*): Unit = send(show"$prefix$aspect:$value|h${tagString(tags: _*)}")

  def histogram(aspect: String, value: Long, tags: String*): Unit = recordHistogramValue(aspect, value, tags: _*)

  def recordEvent(event: Event, tags: String*): Unit = {
    val title: String = StatsdClient.escapeEventString(prefix + event.title)
    val text: String = StatsdClient.escapeEventString(event.text)
    send(show"_e{${title.length},${text.length}:$title|$text${StatsdClient.eventMap(event)}${tagString(tags: _*)}")
  }

  protected def toStatsdString(sc: ServiceCheck): String = {
    val sb: StringBuilder = new StringBuilder
    sb.append(show"_sc|${sc.name}|${sc.status.value}")
    if (sc.timestamp > 0) sb.append(show"|d:${sc.timestamp}")
    if (sc.hostname != null) sb.append(String.format(show"|h:${sc.hostname}"))
    sb.append(tagString(sc.tags: _*))
    if (sc.message != null) sb.append(String.format(show"|m:${sc.escapedMessage}"))
    sb.toString
  }

  def recordServiceCheckRun(sc: ServiceCheck): Unit = send(toStatsdString(sc))

  def serviceCheck(sc: ServiceCheck): Unit = recordServiceCheckRun(sc)

  def recordSetValue(aspect: String, value: String, tags: String*): Unit = send(show"$prefix$aspect:$value|s${tagString(tags: _*)}")
}

object StatsdClient {
  def escapeEventString(title: String): String = title.replace("\n", "\\n")

  private def eventMap(event: Event): String = {
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