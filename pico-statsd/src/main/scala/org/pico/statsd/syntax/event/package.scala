package org.pico.statsd.syntax

import org.pico.event.{Sink, Source}
import org.pico.statsd.{CounterMetric, GaugeMetric, HistogramMetric, StatsdClient}

package object event {
  
  implicit class SourceOps_Common_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def stats(f: (StatsdClient, A) => Unit)
             (implicit c: StatsdClient): Source[A] = {
      self.effect(a => f(c, a))
    }
  }
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def counting(aspect: String, tags: String*)
               (implicit c: StatsdClient): Source[A] = {
      self.effect(a => c.count(aspect, 1, tags: _*))
    }
  
    @inline
    def counting(aspect: String, delta: Long, tags: String*)
               (implicit c: StatsdClient): Source[A] = {
      self.effect(a => c.count(aspect, delta, tags: _*))
    }
  
    @inline
    def viaCounter(aspect: String, tags: String*)
               (implicit c: StatsdClient, m: CounterMetric[A]): Source[A] = {
      self.effect(a => m.send(c, aspect, a, tags.toList))
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def viaGauge(aspect: String, value: A, tags: String*)
                    (implicit c: StatsdClient, m: GaugeMetric[A]): Source[A] = {
      self.effect { a => m.send(c, aspect, a, tags.toList) }
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {

    @inline
    def viaHistogram(aspect: String, value: A, tags: String*)
                 (implicit c: StatsdClient, m: HistogramMetric[A]): Source[A] = {
      self.effect { a => m.send(c, aspect, a, tags.toList) }
    }
  }
}
