package org.pico.statsd

/**
  * Classifies value as a gauge metric.
  * The trait is sealed so only smart constructors can be used to create instances.
  */
sealed trait GaugeMetric[A] {
  def tags(value: A): List[String]
  
  def send(client: StatsdClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object GaugeMetric {
  /**
    * Construct a [[GaugeMetric]] instance that accepts integral values for a given type.
    * @param toValue maps the value to an integral value
    * @param toTags maps the value to value specific tags
    */
  def integral[A](toValue: A => Long, toTags: A => List[String]): GaugeMetric[A] = new GaugeMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, v: A, t: List[String]): Unit =
      client.gauge(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  /**
    * Construct a [[GaugeMetric]] instance that accepts fractional values for a given type.
    * @param toValue maps the value to an fractional value
    * @param toTags maps the value to value specific tags
    */
  def fractional[A](toValue: A => Double, toTags: A => List[String]): GaugeMetric[A] = new GaugeMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, v: A, t: List[String]): Unit =
      client.gauge(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsGaugeMetric    = integral[Int](_.toLong, _ => Nil)
  implicit val longIsGaugeMetric   = integral[Long](identity, _ => Nil)
  implicit val byteIsGaugeMetric   = integral[Byte](_.toLong, _ => Nil)
  implicit val floatIsGaugeMetric  = fractional[Float](_.toDouble, _ => Nil)
  implicit val doubleIsGaugeMetric = fractional[Double](identity, _ => Nil)
}