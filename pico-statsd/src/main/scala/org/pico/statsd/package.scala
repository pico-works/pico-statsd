package org.pico

import org.pico.event.Sink

package object statsd {
  val Ok = Status(0)
  val Warning = Status(1)
  val Critical = Status(2)
  val Unknown = Status(3)
  
  /**
    * Generic StatsD sink. Have a reference to both [[StatsdClient]] and a message
    * and do what you want
    * @param f handle the message using a StatsdClient provided
    */
  def statsSink[A](f: (StatsdClient, A) => Unit)
               (implicit c: StatsdClient): Sink[A] = {
    Sink[A](a => f(c, a))
  }
  
  def counterSink[A](aspect: String, value: A, tags: String*)
                    (implicit c: StatsdClient, m: CounterMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
  
  def gaugeSink[A](aspect: String, value: A, tags: String*)
                  (implicit c: StatsdClient, m: GaugeMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
  
  def histogramSink[A](aspect: String, value: A, tags: String*)
                      (implicit c: StatsdClient, m: HistogramMetric[A]): Sink[A] = {
    Sink[A](a => m.send(c, aspect, a, tags.toList))
  }
}
