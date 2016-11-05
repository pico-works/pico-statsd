package org.pico.statsd

import com.timgroup.statsd.StatsDClient

sealed trait HistogramMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsDClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object HistogramMetric {
  def integral[A](toValue: A => Long, toTags: A => List[String]): HistogramMetric[A] = new HistogramMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, v: A, t: List[String]): Unit =
      client.histogram(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  def fractional[A](toValue: A => Double, toTags: A => List[String]): HistogramMetric[A] = new HistogramMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, v: A, t: List[String]): Unit =
      client.histogram(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsHistogramMetric    = integral[Int](_.toLong, _ => Nil)
  implicit val longIsHistogramMetric   = integral[Long](identity, _ => Nil)
  implicit val byteIsHistogramMetric   = integral[Byte](_.toLong, _ => Nil)
  implicit val floatIsHistogramMetric  = fractional[Float](_.toDouble, _ => Nil)
  implicit val doubleIsHistogramMetric = fractional[Double](identity, _ => Nil)
}
  