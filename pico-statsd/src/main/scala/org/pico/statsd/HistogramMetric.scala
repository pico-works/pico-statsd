package org.pico.statsd

/**
  * Classifies value as a histogram metric.
  * The trait is sealed so only smart constructors can be used to create instances.
  */
sealed trait HistogramMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsdClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object HistogramMetric {
  /**
    * Construct a [[HistogramMetric]] instance that accepts integral values for a given type.
    * @param toValue maps the value to an integral value
    * @param toTags maps the value to value specific tags
    */
  def integral[A](toValue: A => Long, toTags: A => List[String]): HistogramMetric[A] = new HistogramMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, v: A, t: List[String]): Unit =
      client.histogram(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  /**
    * Construct a [[HistogramMetric]] instance that accepts integral values for a given type.
    * @param toValue maps the value to an integral value
    * @param toTags maps the value to value specific tags
    */
  def fractional[A](toValue: A => Double, toTags: A => List[String]): HistogramMetric[A] = new HistogramMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsdClient, aspect: String, v: A, t: List[String]): Unit =
      client.histogram(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsHistogramMetric    = integral[Int](_.toLong, _ => Nil)
  implicit val longIsHistogramMetric   = integral[Long](identity, _ => Nil)
  implicit val byteIsHistogramMetric   = integral[Byte](_.toLong, _ => Nil)
  implicit val floatIsHistogramMetric  = fractional[Float](_.toDouble, _ => Nil)
  implicit val doubleIsHistogramMetric = fractional[Double](identity, _ => Nil)
}
  