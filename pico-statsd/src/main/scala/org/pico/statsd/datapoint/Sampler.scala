package org.pico.statsd.datapoint

import org.pico.statsd.StatsdClient

sealed trait Sampler[A] {
  def sendIn(client: StatsdClient, a: A, tags: List[String] => List[String]): Unit
}

case class IntegralGaugeSampler(aspect: String) extends Sampler[Long] {
  override def sendIn(client: StatsdClient, a: Long, tags: List[String] => List[String]): Unit = {
    client.send(LongGauge(aspect, a, tags(List.empty)))
  }
}

case class FractionalGaugeSampler(aspect: String) extends Sampler[Double] {
  override def sendIn(client: StatsdClient, a: Double, tags: List[String] => List[String]): Unit = {
    client.send(DoubleGauge(aspect, a, tags(List.empty)))
  }
}

case class IntegralHistogramSampler(aspect: String) extends Sampler[Long] {
  override def sendIn(client: StatsdClient, a: Long, tags: List[String] => List[String]): Unit = {
    client.send(LongHistogram(aspect, a, tags(List.empty)))
  }
}

case class FractionalHistogramSampler(aspect: String) extends Sampler[Double] {
  override def sendIn(client: StatsdClient, a: Double, tags: (List[String]) => List[String]): Unit = {
    client.send(DoubleHistogram(aspect, a, tags(List.empty)))
  }
}

case class CounterSampler(aspect: String) extends Sampler[Long] {
  override def sendIn(client: StatsdClient, a: Long, tags: (List[String]) => List[String]): Unit = {
    client.send(DoubleHistogram(aspect, a, tags(List.empty)))
  }
}

case class TimerSampler(aspect: String) extends Sampler[Long] {
  override def sendIn(client: StatsdClient, a: Long, tags: (List[String]) => List[String]): Unit = {
    client.send(Time(aspect, a, tags(List.empty)))
  }
}
