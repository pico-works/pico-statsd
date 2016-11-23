package org.pico

import org.pico.event.Sink

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
  
  def metricsSink[A](aspect: String, sampleRate: Option[SampleRate], tags: String*)
                    (implicit c: StatsdClient, m: Metric[A]): Sink[A] = {
    Sink[A](sendMetrics(c, aspect, sampleRate.getOrElse(SampleRate.always), tags.toList, m))
  }
  
  
  def counterSink[A](aspect: String, sampleRate: Option[SampleRate], delta: Long, tags: String*)
                     (implicit c: StatsdClient): Sink[A] = {
    Sink[A](a => c.count(aspect, delta, sampleRate.getOrElse(SampleRate.always), tags: _*))
  }
  
  def counterSink[A](aspect: String, sampleRate: Option[SampleRate], tags: String*)
                     (implicit c: StatsdClient): Sink[A] = {
    counterSink[A](aspect, sampleRate, 1, tags: _*)
  }
  
  private[statsd] def sendMetrics[A](c: StatsdClient, prefix: String, sampleRate: SampleRate, extraTags: List[String], m: Metric[A])(value: A): Unit = {
    def fullAspectName(aspect: String) = if (prefix == null || prefix.isEmpty) aspect else prefix + "." + aspect
    
    val tags = extraTags ++ m.tags(value)
    m.values(value).foreach {
      case IntegralGauge(aspect, v) =>
        c.gauge(fullAspectName(aspect), v, sampleRate, tags: _*)
      
      case FractionalGauge(aspect, v) =>
        c.gauge(fullAspectName(aspect), v, sampleRate, tags: _*)
      
      case IntegralHistogram(aspect, v) =>
        c.histogram(fullAspectName(aspect), v, sampleRate, tags: _*)
      
      case FractionalHistogram(aspect, v) =>
        c.histogram(fullAspectName(aspect), v, sampleRate, tags: _*)
      
      case Counter(aspect, v) =>
        c.count(fullAspectName(aspect), v, sampleRate, tags: _*)

      case Timer(aspect, v) =>
        c.time(fullAspectName(aspect), v.toMillis, sampleRate, tags: _*)
    }
  }
}
