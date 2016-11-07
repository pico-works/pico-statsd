package org.pico.statsd.syntax

import com.timgroup.statsd.StatsDClient
import org.pico.disposal.std.autoCloseable._
import org.pico.event.{SinkSource, Source}
import org.pico.statsd.{CounterMetric, GaugeMetric, HistogramMetric}

package object event {
  
  implicit class SinkSourceOps_Common_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def stats(f: (StatsDClient, B) => Unit)
             (implicit c: StatsDClient): SinkSource[A, B] = {
      self += self.subscribe(x => f(c, x))
      self
    }
  }
  
  implicit class SourceOps_Common_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def stats(f: (StatsDClient, A) => Unit)
             (implicit c: StatsDClient): Source[A] = {
      self += self.subscribe(a => f(c, a))
      self
    }
  }
  
  implicit class SinkSourceOps_Counter_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
   
    @inline
    def withCounter(aspect: String, delta: Long, tags: String*)
                   (implicit c: StatsDClient): SinkSource[A, B] = {
      self += self.subscribe(a => c.count(aspect, delta, tags: _*))
      self
    }
  
    @inline
    def withCounter(aspect: String, tags: String*)
                   (implicit c: StatsDClient, m: CounterMetric[B]): SinkSource[A, B] = {
      self += self.subscribe(x => m.send(c, aspect, x, tags.toList))
      self
    }
  }
  
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {

    @inline
    def withCounter(aspect: String, delta: Long, tags: String*)
                   (implicit c: StatsDClient): Source[A] = {
      self += self.subscribe(a => c.count(aspect, delta, tags: _*))
      self
    }

    @inline
    def withCounter(aspect: String, tags: String*)
                   (implicit c: StatsDClient, m: CounterMetric[A]): Source[A] = {
      self += self.subscribe(a => m.send(c, aspect, a, tags.toList))
      self
    }
  }
  
  implicit class SinkSourceOps_Gauge_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    
    @inline
    def withGauge(aspect: String, tags: String*)
                 (implicit c: StatsDClient, m: GaugeMetric[B]): SinkSource[A, B] = {
      self += self.subscribe { x => m.send(c, aspect, x, tags.toList) }
      self
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withGauge(aspect: String, tags: String*)
                 (implicit c: StatsDClient, m: GaugeMetric[A]): Source[A] = {
      self += self.subscribe { x => m.send(c, aspect, x, tags.toList) }
      self
    }
  }
  
  implicit class SinkSourceOps_Histogram_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {

    @inline
    def withHistogram(aspect: String, tags: String*)
                     (implicit c: StatsDClient, m: HistogramMetric[B]): SinkSource[A, B] = {
      self += self.subscribe { x => m.send(c, aspect, x, tags.toList) }
      self
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {

    @inline
    def withHistogram(aspect: String, tags: String*)
                     (implicit c: StatsDClient, m: HistogramMetric[A]): Source[A] = {
      self += self.subscribe { x => m.send(c, aspect, x, tags.toList) }
      self
    }
  }
}
