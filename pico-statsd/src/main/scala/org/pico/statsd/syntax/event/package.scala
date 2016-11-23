package org.pico.statsd.syntax

import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, SinkSource, Source}
import org.pico.statsd._
import org.pico.statsd.datapoint._

import scala.concurrent.duration.Deadline

package object event {
  
  //-------------------- METRIC --------------------------------------------
  implicit class SinkSourceOps_Metric_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: Sampler[B]): SinkSource[A, B] = {
      implicit val samplingB = Sampling[B](sampleRate)
      self += self.effect(a => c.sample[B](a))
      self
    }
  }
  
  implicit class SourceOps_Metric_Rht98nT[A](val self: Source[A]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: Sampler[A]): Source[A] = {
      implicit val samplingB = Sampling[A](sampleRate)
      self += self.effect(a => c.sample[A](a))
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
    def withSimpleTimer(aspect: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val tagsArray = tags.toArray
      val bus = Bus[B]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.send(aspect, SampleRated(sampleRate, Time(aspect, (Deadline.now - start).toMillis)), tagsArray)
        }
      })
      
      SinkSource.from(self, bus)
    }
    
    @inline
    def withTimer(aspect: String, sampleRate: SampleRate, tags: String*)
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
    def withSimpleTimer(aspect: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          c.send(aspect, SampleRated(sampleRate, Time(aspect, (Deadline.now - start).toMillis)), tags)
        }
      })
      bus
    }
    
    @inline
    def withTimer(aspect: String, sampleRate: SampleRate, tags: String*)
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
    def withCounter(aspect: String, delta: Long, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.subscribe(a => c.send(aspect, SampleRated(sampleRate, Count(aspect, delta)), tags))
      self
    }
  
    @inline
    def withCounter(aspect: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.subscribe(a => c.send(aspect, SampleRated(sampleRate, Count(aspect, 1L)), tags))
      self
    }
  }
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withCounter(aspect: String, delta: Long, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, Count(aspect, delta)), tags))
      self
    }
    
    @inline
    def withCounter(aspect: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, Count(aspect, 1L)), tags))
      self
    }
  }
  
  //-------------------- GAUGES --------------------------------------------
  implicit class SinkSourceOps_Gauge_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralGauge(aspect: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongGauge(value(a))), tags))
      self
    }
  
    @inline
    def withFractionalGauge(aspect: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongGauge(value(a))), tags))
      self
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralGauge(aspect: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongGauge(value(a))), tags))
      self
    }
    
    @inline
    def withFractionalGauge(aspect: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                 (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, DoubleGauge(value(a))), tags))
      self
    }
  }
  
  //-------------------- HISTOGRAMS ------------------------------------------
  implicit class SinkSourceOps_Histogram_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(aspect: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongHistogram(value(a))), tags))
      self
    }
  
    @inline
    def withFractionalHistogram(aspect: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): SinkSource[A, B] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongHistogram(value(a))), tags))
      self
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(aspect: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                             (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, LongHistogram(value(a))), tags))
      self
    }
  
    @inline
    def withFractionalHistogram(aspect: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                               (implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.send(aspect, SampleRated(sampleRate, DoubleHistogram(value(a))), tags))
      self
    }
  }
}
