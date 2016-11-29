package org.pico.statsd.syntax

import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, SinkSource, Source}
import org.pico.statsd._
import org.pico.statsd.datapoint.{IntegralHistogramMetric, _}

import scala.concurrent.duration.Deadline

package object event {
  //-------------------- METRIC --------------------------------------------
  implicit class SinkSourceOps_Metric_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: Metric[B]): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate).withAspect(aspect)
      val m2 = Metric[B](m, TaggedWith[B](tags: _*))
      self += self.effect { a =>
        configuredClient.sample[B](a)(m2)
      }
      self
    }
  }
  
  implicit class SourceOps_Metric_Rht98nT[A](val self: Source[A]) extends AnyVal {
    def withMetrics(aspect: String, sampleRate: SampleRate, tags: String*)(implicit c: StatsdClient, m: Metric[A]): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate).withAspect(aspect)
      val m2 = Metric[A](m, TaggedWith[A](tags: _*))
      self += self.effect { a =>
        configuredClient.sample[A](a)(m2)
      }
      self
    }
  }
  
  //-------------------- COMMON --------------------------------------------
  implicit class SinkSourceOps_Common_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def withStats(f: (StatsdClient, B) => Unit)(implicit c: StatsdClient): SinkSource[A, B] = {
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
      val bus = Bus[B]
      val sampler = Metric[Time](TimerMetric(metric), TaggedWith(tags: _*))
      bus += self.subscribe { a =>
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          configuredClient.sample(Time((Deadline.now - start).toMillis))(sampler)
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
      val sampler = Metric[Time](TimerMetric(metric), TaggedWith(tags: _*))
      bus += self.subscribe(a => {
        val start = Deadline.now
        try {
          bus.publish(a)
        } finally {
          configuredClient.sample(Time((Deadline.now - start).toMillis))(sampler)
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
      val sampler = Metric[B](AddMetric(metric, delta), TaggedWith(tags: _*))
      self += self.subscribe(a => configuredClient.sample(a)(sampler))
      self
    }
  
    @inline
    def withCounter(metric: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate).withAspect(metric)
      val sampler = Metric[Increment](CountMetric(metric), TaggedWith(tags: _*))
      self += self.subscribe(a => configuredClient.sample(Increment())(sampler))
      self
    }
  }
  
  implicit class SourceOps_Counter_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def withCounter(metric: String, delta: Long, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[Count](CountMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(Count(delta))(sampler))
      self
    }
    
    @inline
    def withCounter(metric: String, sampleRate: SampleRate, tags: String*)
                   (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[Increment](CountMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(Increment())(sampler))
      self
    }
  }
  
  //-------------------- GAUGES --------------------------------------------
  implicit class SinkSourceOps_Gauge_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def withIntegralGauge(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[LongGauge](IntegralGaugeMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(LongGauge(value(a)))(sampler))
      self
    }
  
    @inline
    def withFractionalGauge(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[DoubleGauge](FractionalGaugeMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(DoubleGauge(value(a)))(sampler))
      self
    }
  }
  
  implicit class SourceOps_Gauge_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def withIntegralGauge(metric: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[LongGauge](IntegralGaugeMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(LongGauge(value(a)))(sampler))
      self
    }

    @inline
    def withFractionalGauge(metric: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                 (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[DoubleGauge](FractionalGaugeMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(DoubleGauge(value(a)))(sampler))
      self
    }
  }
  
  //-------------------- HISTOGRAMS ------------------------------------------
  implicit class SinkSourceOps_Histogram_Rht98nT[A, B](val self: SinkSource[A, B]) extends AnyVal {
    @inline
    def withIntegralHistogram(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                         (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[LongHistogram](IntegralHistogramMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(LongHistogram(value(a)))(sampler))
      self
    }
  
    @inline
    def withFractionalHistogram(metric: String, value: B => Long, sampleRate: SampleRate, tags: String*)
                           (implicit c: StatsdClient): SinkSource[A, B] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[DoubleHistogram](FractionalHistogramMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(DoubleHistogram(value(a)))(sampler))
      self
    }
  }
  
  implicit class SourceOps_Histogram_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def withIntegralHistogram(metric: String, value: A => Long, sampleRate: SampleRate, tags: String*)
                             (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[LongHistogram](IntegralHistogramMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(LongHistogram(value(a)))(sampler))
      self
    }
  
    @inline
    def withFractionalHistogram(metric: String, value: A => Double, sampleRate: SampleRate, tags: String*)
                               (implicit c: StatsdClient): Source[A] = {
      val configuredClient = c.sampledAt(sampleRate)
      val sampler = Metric[DoubleHistogram](FractionalHistogramMetric(metric), TaggedWith(tags: _*))
      self += self.effect(a => configuredClient.sample(DoubleHistogram(value(a)))(sampler))
      self
    }
  }

  implicit class SourceOps_MetricThings_Rht98nT[A](val self: Source[A]) extends AnyVal {
    @inline
    def withMetric(metric: Metric[A])(implicit c: StatsdClient): Source[A] = {
      self += self.effect(a => c.sample(a)(metric))
      self
    }
  }
}
