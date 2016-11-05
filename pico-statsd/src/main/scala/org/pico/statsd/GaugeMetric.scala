package org.pico.statsd

import com.timgroup.statsd.StatsDClient

sealed trait GaugeMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsDClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object GaugeMetric {
  def integral[A](toValue: A => Long, toTags: A => List[String]): GaugeMetric[A] = new GaugeMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, v: A, t: List[String]): Unit =
      client.gauge(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  def fractional[A](toValue: A => Double, toTags: A => List[String]): GaugeMetric[A] = new GaugeMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, v: A, t: List[String]): Unit =
      client.gauge(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsGaugeMetric    = integral[Int](_.toLong, _ => Nil)
  implicit val longIsGaugeMetric   = integral[Long](identity, _ => Nil)
  implicit val byteIsGaugeMetric   = integral[Byte](_.toLong, _ => Nil)
  implicit val floatIsGaugeMetric  = fractional[Float](_.toDouble, _ => Nil)
  implicit val doubleIsGaugeMetric = fractional[Double](identity, _ => Nil)
}