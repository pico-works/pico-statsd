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
      val configuredClient = c.sampledAt(sampleRate).withAspect(aspect)
      self += self.effect(a => configuredClient.sample[B](a))
      self
    }
  }
  
  implicit class SourceOps_Metric_Rht98nT[A](val self: Source[A]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: Sampler[A]): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate).withAspect(aspect)
      self += self.effect(a => configuredClient.sampledAt(sampleRate).sample[A](a))
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
    def stats(f: (StatsdClient, A) => Unit)(implicit c: StatsdClient): Source[A] = {
      self += self.subscribe(a => f(c, a))
      self
    }
  }
  
  //-------------------- Timers -------------------------------------------
  implicit class SinkSourceOps_Timer_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    
    @inline
    def withSimpleTimer(metric: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      val tagsArray = tags.toArray
      val bus = Bus[B]
      bus += self.subscribe { a =>
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          configuredClient.send(metric, Time((Deadline.now - start).toMillis), tagsArray)
        }
      }
      
      SinkSource.from(self, bus)
    }
    
    @inline
    def withTimer(metric: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: TimerMetric[B]): SinkSource[A, B] = {
      val bus = Bus[B]
      bus += self.subscribe { a =>
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          // TODO Is it intended to use send?
          m.send(c, metric, a, Deadline.now - start, sampleRate, tags.toList)
        }
      }
  
      SinkSource.from(self, bus)
    }
  }
  
  
  implicit class SourceOps_Timer_Rht98nT[A](val self: Source[A]) extends AnyVal {
    
    @inline
    def withSimpleTimer(metric: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          configuredClient.send(metric, Time((Deadline.now - start).toMillis), tags)
        }
      })
      bus
    }
    
    @inline
    def withTimer(metric: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient, m: TimerMetric[A]): Source[A] = {
      val bus = Bus[A]
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          // TODO Is it intended to use send?
          m.send(c, metric, a, Deadline.now - start, sampleRate, tags.toList)
        }
      })
      bus
    }
  }
  
  //-------------------- COUNTERS -------------------------------------------
  implicit class SinkSourceOps_Counter_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
   
    @inline
    def withCounter(metric: String, delta: Long, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.subscribe(a => configuredClient.send(metric, Count(delta), tags))
      self
    }
  
    @inline
    def withCounter(metric: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.subscribe(a => configuredClient.send(metric, Increment(), tags))
      self
    }
  }
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {

    @inline
    def withCounter(metric: String, delta: Long, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, Count(delta), tags))
      self
    }
    
    @inline
    def withCounter(metric: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, Increment(), tags))
      self
    }
  }
  
  //-------------------- GAUGES --------------------------------------------
  implicit class SinkSourceOps_Gauge_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralGauge(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongGauge(value(a)), tags))
      self
    }
  
    @inline
    def withFractionalGauge(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongGauge(value(a)), tags))
      self
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralGauge(metric: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongGauge(value(a)), tags))
      self
    }
    
    @inline
    def withFractionalGauge(metric: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                 (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, DoubleGauge(value(a)), tags))
      self
    }
  }
  
  //-------------------- HISTOGRAMS ------------------------------------------
  implicit class SinkSourceOps_Histogram_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongHistogram(value(a)), tags))
      self
    }
  
    @inline
    def withFractionalHistogram(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongHistogram(value(a)), tags))
      self
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {
  
    @inline
    def withIntegralHistogram(metric: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                             (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, LongHistogram(value(a)), tags))
      self
    }
  
    @inline
    def withFractionalHistogram(metric: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                               (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      self += self.effect(a => configuredClient.send(metric, DoubleHistogram(value(a)), tags))
      self
    }
  }
}
