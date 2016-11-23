package org.pico.statsd.syntax

import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, SinkSource, Source}
import org.pico.statsd._

import scala.concurrent.duration.Deadline

package object event {
  
  //-------------------- METRIC --------------------------------------------
  implicit class SinkSourceOps_Metric_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: Option[SampleRate], tags: String*)(implicit c: StatsdClient, m: Metric[B]): SinkSource[A, B] = {
      self += self.effect(c.sendMetrics(aspect, sampleRate.getOrElse(SampleRate.always), tags.toList, m))
      self
    }
  }
  
  implicit class SourceOps_Metric_Rht98nT[A](val self: Source[A]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: Option[SampleRate], tags: String*)(implicit c: StatsdClient, m: Metric[A]): Source[A] = {
      self += self.effect(c.sendMetrics(aspect, sampleRate.getOrElse(SampleRate.always), tags.toList, m))
      self
    }
  }
  
  //-------------------- COMMON --------------------------------------------
  implicit class SinkSourceOps_Common_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def withStats(f: (StatsdClient, B) => Unit)
             (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.subscribe(x => f(c, x))
      self
    }
  }
  
  implicit class SourceOps_Common_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def stats(f: (StatsdClient, A) => Unit)
             (implicit c: StatsdClient): Source[A] = {
      self += self.subscribe(a => f(c, a))
      self
    }
  }
  
  //-------------------- Timers -------------------------------------------
  implicit class SinkSourceOps_Timer_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    
    @inline
    def withSimpleTimer(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      val bus = Bus[B]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.time(aspect, (Deadline.now - start).toMillis, sampleRate.getOrElse(SampleRate.always), tagsArray: _*)
        }
      })
      
      SinkSource.from(self, bus)
    }
    
    @inline
    def withTimer(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient, m: TimerMetric[B]): SinkSource[A, B] = {
      val bus = Bus[B]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          m.send(c, aspect, a,  Deadline.now - start, sampleRate, tags.toList)
        }
      })
  
      SinkSource.from(self, bus)
    }
  }
  
  
  implicit class SourceOps_Timer_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withSimpleTimer(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.time(aspect, (Deadline.now - start).toMillis, sampleRate.getOrElse(SampleRate.always), tagsArray: _*)
        }
      })
      bus
    }
    
    @inline
    def withTimer(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient, m: TimerMetric[A]): Source[A] = {
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          m.send(c, aspect, a, Deadline.now - start, sampleRate, tags.toList)
        }
      })
      bus
    }
  }
  
  //-------------------- COUNTERS -------------------------------------------
  implicit class SinkSourceOps_Counter_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
   
    @inline
    def withCounter(aspect: String, delta: Long, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe(a => c.count(aspect, delta, sampleRate.getOrElse(SampleRate.always), tagsArray: _*))
      self
    }
  
    @inline
    def withCounter(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe(a => c.count(aspect, 1, sampleRate.getOrElse(SampleRate.always), tagsArray: _*))
      self
    }
  }
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withCounter(aspect: String, delta: Long, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect(a => c.count(aspect, delta, sampleRate.getOrElse(SampleRate.always), tagsArray: _*))
      self
    }
    
    @inline
    def withCounter(aspect: String, sampleRate: Option[SampleRate], tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect(a => c.count(aspect, 1, sampleRate.getOrElse(SampleRate.always), tagsArray: _*))
      self
    }
  }
  
  //-------------------- GAUGES --------------------------------------------
  implicit class SinkSourceOps_Gauge_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralGauge(aspect: String, value: B => Long, sampleRate: Option[SampleRate], tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe { x => c.gauge(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  
    @inline
    def withFractionalGauge(aspect: String, value: B => Long, sampleRate: Option[SampleRate], tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe { x => c.gauge(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralGauge(aspect: String, value: A => Long, sampleRate: Option[SampleRate], tags: String*)
                           (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect { x => c.gauge(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
    
    @inline
    def withFractionalGauge(aspect: String, value: A => Double, sampleRate: Option[SampleRate], tags: String*)
                 (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect { x => c.gauge(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  }
  
  //-------------------- HISTOGRAMS ------------------------------------------
  implicit class SinkSourceOps_Histogram_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(aspect: String, value: B => Long, sampleRate: Option[SampleRate], tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe { x => c.histogram(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  
    @inline
    def withFractionalHistogram(aspect: String, value: B => Long, sampleRate: Option[SampleRate], tags: String*)
                           (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      self += self.subscribe { x => c.histogram(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(aspect: String, value: A => Long, sampleRate: Option[SampleRate], tags: String*)
                             (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect { x => c.histogram(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  
    @inline
    def withFractionalHistogram(aspect: String, value: A => Double, sampleRate: Option[SampleRate], tags: String*)
                               (implicit c: StatsdClient): Source[A] = {
      val tagsArray = tags.toArray
      self += self.effect { x => c.histogram(aspect, value(x), sampleRate.getOrElse(SampleRate.always), tagsArray: _*) }
      self
    }
  }
}
