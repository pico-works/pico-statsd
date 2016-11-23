package org.pico

import org.pico.event.Sink
import org.pico.statsd.datapoint.{Count, Sampler, Sampling}

package object statsd {
  /**
    * Generic StatsD sink. Have a reference to both [[StatsdClient]] and a message
    * and do what you want
    * @param f handle the message using a StatsdClient provided
    */
  def statsSink[A](f: (StatsdClient, A) => Unit)
               (implicit c: StatsdClient): Sink[A] = {
    Sink[A](a => f(c, a))
  }
  
  def metricsSink[A: Sampler](aspect: String, sampleRate: SampleRate, tags: String*)
                    (implicit c: StatsdClient, m: Metric[A]): Sink[A] = {
    Sink[A](c.sample[A])
  }
  
  def counterSink[A](aspect: String, sampleRate: SampleRate, delta: Long, tags: String*)
                     (implicit c: StatsdClient): Sink[A] = {
    val tagList = tags.toList
    Sink[A] { a =>
      c.sampledAt(sampleRate).send(aspect, Count(delta), tagList)
    }
  }
  
  def counterSink[A](aspect: String, sampleRate: SampleRate, tags: String*)
                     (implicit c: StatsdClient): Sink[A] = {
    counterSink[A](aspect, sampleRate, 1, tags: _*)
  }
}
