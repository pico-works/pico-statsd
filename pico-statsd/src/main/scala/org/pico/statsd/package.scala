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
  
  def metricsSink[A](aspect: String, tags: String*)
                    (implicit c: StatsDClient, m: Metric[A]): Sink[A] = {
    Sink[A](sendMetrics(c, aspect, tags.toList, m))
  }
  
  
  def counterSink[A](aspect: String, delta: Long, tags: String*)
                     (implicit c: StatsDClient): Sink[A] = {
    Sink[A](a => c.count(aspect, delta, tags: _*))
  }
  
  def counterSink[A](aspect: String, tags: String*)
                     (implicit c: StatsDClient): Sink[A] = {
    counterSink[A](aspect, 1, tags: _*)
  }
  
  private[statsd] def sendMetrics[A](c: StatsDClient, prefix: String, extraTags: List[String], m: Metric[A])(value: A): Unit = {
    def fullAspectName(aspect: String) = if (prefix == null || prefix.isEmpty) aspect else prefix + "." + aspect
    
    val tags = extraTags ++ m.tags(value)
    m.values(value).foreach {
      case IntegralGauge(aspect, v) =>
        c.gauge(fullAspectName(aspect), v, tags: _*)
      
      case FractionalGauge(aspect, v) =>
        c.gauge(fullAspectName(aspect), v, tags: _*)
      
      case IntegralHistogram(aspect, v) =>
        c.histogram(fullAspectName(aspect), v, tags: _*)
      
      case FractionalHistogram(aspect, v) =>
        c.histogram(fullAspectName(aspect), v, tags: _*)
      
      case Counter(aspect, v) =>
        c.count(fullAspectName(aspect), v, tags: _*)

      case Timer(aspect, v) =>
        c.time(fullAspectName(aspect), v, tags: _*)
    }
  }
}
