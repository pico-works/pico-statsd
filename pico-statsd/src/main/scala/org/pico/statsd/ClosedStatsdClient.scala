package org.pico.statsd
import org.pico.event.{ClosedSource, Source}

class ClosedStatsdClient extends StatsdClient {
  override def errors: Source[Exception] = ClosedSource

  override def send(message: String): Unit = ()

  override def prefix: String = ""

  override def tagString(tags: String*): String = ""

  override def close(): Unit = ()

  override def count(aspect: String, delta: Long, tags: String*): Unit = ()

  override def incrementCounter(aspect: String, tags: String*): Unit = ()

  override def increment(aspect: String, tags: String*): Unit = ()

  override def decrementCounter(aspect: String, tags: String*): Unit = ()

  override def decrement(aspect: String, tags: String*): Unit = ()

  override def recordGaugeValue(aspect: String, value: Double, tags: String*): Unit = ()

  override def gauge(aspect: String, value: Double, tags: String*): Unit = ()

  override def recordGaugeValue(aspect: String, value: Long, tags: String*): Unit = ()

  override def gauge(aspect: String, value: Long, tags: String*): Unit = ()

  override def recordExecutionTime(aspect: String, timeInMs: Long, tags: String*): Unit = ()

  override def time(aspect: String, value: Long, tags: String*): Unit = ()

  override def recordHistogramValue(aspect: String, value: Double, tags: String*): Unit = ()

  override def histogram(aspect: String, value: Double, tags: String*): Unit = ()

  override def recordHistogramValue(aspect: String, value: Long, tags: String*): Unit = ()

  override def histogram(aspect: String, value: Long, tags: String*): Unit = ()

  override def recordEvent(event: Event, tags: String*): Unit = ()

  override def recordServiceCheckRun(sc: ServiceCheck): Unit = ()

  override def serviceCheck(sc: ServiceCheck): Unit = ()

  override def recordSetValue(aspect: String, value: String, tags: String*): Unit = ()
}
