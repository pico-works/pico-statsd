package org.pico

import com.timgroup.statsd.StatsDClient
import org.pico.event.Sink

package object statsd {
  /**
    * Generic StatsD sink. Have a reference to both [[StatsDClient]] and a message
    * and do what you want
    * @param f handle the message using a StatsDClient provided
    */
  def statsSink[A](f: (StatsDClient, A) => Unit)
               (implicit c: StatsDClient): Sink[A] = {
    Sink[A](a => f(c, a))
  }
  
  def counterSink[A](aspect: String, tags: String*)
                    (implicit c: StatsDClient, m: CounterMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
  
  def countingSink[A](aspect: String, delta: Long, tags: String*)
                     (implicit c: StatsDClient): Sink[A] = {
    Sink[A](a => c.count(aspect, delta, tags: _*))
  }
  
  def countingSink[A](aspect: String, tags: String*)
                     (implicit c: StatsDClient): Sink[A] = {
    countingSink[A](aspect, 1, tags: _*)
  }
  
  def gaugeSink[A](aspect: String, tags: String*)
                  (implicit c: StatsDClient, m: GaugeMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
  
  def histogramSink[A](aspect: String, tags: String*)
                      (implicit c: StatsDClient, m: HistogramMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
}
