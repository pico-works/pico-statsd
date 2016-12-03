package org.pico

import org.pico.event.Sink

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
  
  def counterSink[A](metric: String, sampleRate: SampleRate, delta: Long, tags: String*)(implicit c: StatsdClient): Sink[A] = {
    val configuredClient = c.sampledAt(sampleRate)
    val sampler = Metric[A](AddMetric(metric, delta), TaggedWith(tags: _*))
    Sink[A](a => configuredClient.sample(a)(sampler))
  }
  
  def counterSink[A](metric: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient): Sink[A] = {
    val configuredClient = c.sampledAt(sampleRate)
    val sampler = Metric[A](CountMetric(metric), TaggedWith(tags: _*))
    Sink[A](a => configuredClient.sample(a)(sampler))
  }
}
