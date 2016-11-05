package org.pico.statsd

import com.timgroup.statsd.StatsDClient

sealed trait CounterMetric[A] {
  def tags(value: A): List[String]
  def send(client: StatsDClient, aspect: String, value: A, extraTags: List[String]): Unit
}

object CounterMetric {
  def integral[A](toValue: A => Long, toTags: A => List[String]): CounterMetric[A] = new CounterMetric[A] {
    def tags(value: A) = toTags(value)
    def send(client: StatsDClient, aspect: String, v: A, t: List[String]): Unit =
      client.count(aspect, toValue(v), t ++ tags(v): _*)
  }
  
  implicit val intIsCounterMetric  = integral[Int](_.toLong, _ => Nil)
  implicit val longIsCounterMetric = integral[Long](identity, _ => Nil)
  implicit val byteIsCounterMetric = integral[Byte](_.toLong, _ => Nil)
}