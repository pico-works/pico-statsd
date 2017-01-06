package org.pico

import org.pico.event.Sink
import org.pico.statsd.datapoint._

package object statsd {
  /**
    * Generic StatsD sink. Have a reference to both [[StatsdClient]] and a message
    * and do what you want
    * @param f handle the message using a StatsdClient provided
    */
  def statsSink[A](f: (StatsdClient, A) => Unit)(implicit c: StatsdClient): Sink[A] = {
    Sink[A](a => f(c, a))
  }
  
  def metricsSink[A: Metric](aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient): Sink[A] = {
    val configuredClient = c.sampledAt(sampleRate).withAspect(aspect)
    Sink[A](configuredClient.sample[A])
  }
  
  def alertSink[A](aspect: String, tags: String*)(implicit c: StatsdClient, a: Alert[A]): Sink[A] = {
    val configuredClient = c.withAspect(aspect)
    val sampler = Alert[A](a, EventTaggedWith(tags.toList))
    Sink[A](x => configuredClient.alert(x)(sampler))
  }
  
  def eventSink(title: String, tags: String*)(implicit c: StatsdClient): Sink[EventData] = {
    val sampler: Alert[EventData] = Alert[EventData](Event(title), EventTaggedWith(tags.toList))
    Sink[EventData](x => c.alert[EventData](x)(sampler))
  }
  
  def counterSink[A](metric: String, sampleRate: SampleRate, delta: Long, tags: String*)(implicit c: StatsdClient): Sink[A] = {
    val configuredClient = c.sampledAt(sampleRate)
    val sampler = Metric[A](AddMetric(metric, delta), TaggedWith(tags.toList))
    Sink[A](a => configuredClient.sample(a)(sampler))
  }
  
  def counterSink[A](metric: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient): Sink[A] = {
    val configuredClient = c.sampledAt(sampleRate)
    val sampler = Metric[A](IncrementMetric(metric), TaggedWith(tags.toList))
    Sink[A](a => configuredClient.sample(a)(sampler))
  }
}
