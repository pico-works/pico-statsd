package org.pico.statsd.syntax

import com.timgroup.statsd.StatsDClient
import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, SinkSource, Source}
import org.pico.statsd.{CounterMetric, GaugeMetric, HistogramMetric, TimerMetric}

import scala.concurrent.duration.Deadline

package object event {
  
  //-------------------- COMMON --------------------------------------------
  implicit class SinkSourceOps_Common_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def withStats(f: (StatsDClient, B) => Unit)
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
  
  //-------------------- Timers -------------------------------------------
  implicit class SinkSourceOps_Timer_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    
    @inline
    def withSimpleTimer(aspect: String, tags: String*)
                   (implicit c: StatsDClient): SinkSource[A, B] = {
      val bus = Bus[B]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.time(aspect, (Deadline.now - start).toMillis, tags: _*)
        }
      })
      
      SinkSource.from(self, bus)
    }
    
    @inline
    def withTimer(aspect: String, tags: String*)
                   (implicit c: StatsDClient, m: TimerMetric[B]): SinkSource[A, B] = {
      val bus = Bus[B]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          m.send(c, aspect, a, Deadline.now - start, tags.toList)
        }
      })
  
      SinkSource.from(self, bus)
    }
  }
  
  
  implicit class SourceOps_Timer_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withSimpleTimer(aspect: String, tags: String*)
                   (implicit c: StatsDClient): Source[A] = {
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.time(aspect, (Deadline.now - start).toMillis, tags: _*)
        }
      })
      bus
    }
    
    @inline
    def withTimer(aspect: String, tags: String*)
                   (implicit c: StatsDClient, m: TimerMetric[A]): Source[A] = {
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          m.send(c, aspect, a, Deadline.now - start, tags.toList)
        }
      })
      bus
    }
  }
  
  //-------------------- COUNTERS -------------------------------------------
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
  
  //-------------------- GAUGES --------------------------------------------
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
  
  //-------------------- HISTOGRAMS ------------------------------------------
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
