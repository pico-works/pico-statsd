package org.pico.statsd

/**
  * Classifies value as a counter metric.
  * The trait is sealed so only smart constructors can be used to create instances.
  */
sealed trait CounterMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsdClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object CounterMetric {
  /**
    * Construct [[CounterMetric]] instance for a given type.
    * Counters can only accept integral values
    * @param toValue maps the value to counter value
    * @param toTags maps the value to value specific tags
    */
  def integral[A](toValue: A => Long, toTags: A => List[String]): CounterMetric[A] = new CounterMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, v: A, t: List[String]): Unit =
      client.count(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsCounterMetric  = integral[Int](_.toLong, _ => Nil)
  implicit val longIsCounterMetric = integral[Long](identity, _ => Nil)
  implicit val byteIsCounterMetric = integral[Byte](_.toLong, _ => Nil)
}