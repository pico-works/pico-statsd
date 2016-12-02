package org.pico.statsd.datapoint

import org.pico.statsd.{StatsdClient, SampleRate, StatsdConfig}
import org.pico.statsd.syntax.metric._

@specialized(Long, Double)
trait Metric[-A] { self =>
  def configure(config: StatsdConfig): StatsdConfig = config

  def constantTags: List[String]

  def deriveTags(a: A, tags: List[String]): List[String]

  def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit

  def comap[B](f: B => A): Metric[B] = {
    new Metric[B] {
      override def constantTags: List[String] = self.constantTags

      override def deriveTags(b: B, tags: List[String]): List[String] = self.deriveTags(f(b), tags)

      override def sendIn(client: StatsdClient, b: B, tags: List[String]): Unit = self.sendIn(client, f(b), tags)
    }
  }

  final def sendIn(client: StatsdClient, a: A): Unit = sendIn(client, a, deriveTags(a, constantTags))
}

object Metric {
  def empty[A]: Metric[A] = new Metric[A] {
    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }

  def of[A: Metric]: Metric[A] = implicitly[Metric[A]]

  def apply[A](metrics: Metric[A]*): Metric[A] = (empty[A] /: metrics)(_ :+: _)

  def append[A](self: Metric[A], that: Metric[A]): Metric[A] = {
    new Metric[A] {
      override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
        self.sendIn(client, a, tags)
        that.sendIn(client, a, tags)
      }

      lazy val constantTags: List[String] = self.constantTags ++ that.constantTags

      override def deriveTags(a: A, tags: List[String]): List[String] = that.deriveTags(a, self.deriveTags(a, tags))
    }
  }
}

case class SumMetric[A](metric: String) extends Metric[Long] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: Long, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: Long, tags: List[String]): Unit = {
    client.send(client.config, metric, Count(a), tags)
  }
}

case class AddMetric[A](metric: String, delta: Long) extends Metric[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(client.config, metric, Count(delta), tags)
  }
}

object CountMetric {
  def apply[A](metric: String): Metric[A] = new Metric[A] {
    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
      client.send(client.config, metric, Increment(), tags)
    }
  }
}

case class DecrementMetric[A](metric: String) extends Metric[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(client.config, metric, Decrement(), tags)
  }
}

case class IntegralGaugeMetric(metric: String) extends Metric[LongGauge] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: LongGauge, tags: List[String]): Unit = {
    client.send(client.config, metric, a, tags)
  }

  override def deriveTags(a: LongGauge, tags: List[String]): List[String] = tags
}

case class FractionalGaugeMetric(metric: String) extends Metric[DoubleGauge] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: DoubleGauge, tags: List[String]): Unit = {
    client.send(client.config, metric, a, tags)
  }

  override def deriveTags(a: DoubleGauge, tags: List[String]): List[String] = tags
}

case class IntegralHistogramMetric(metric: String) extends Metric[LongHistogram] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: LongHistogram, tags: List[String]): Unit = {
    client.send(client.config, metric, a, tags)
  }

  override def deriveTags(a: LongHistogram, tags: List[String]): List[String] = tags
}

case class FractionalHistogramMetric(metric: String) extends Metric[DoubleHistogram] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: DoubleHistogram, tags: List[String]): Unit = {
    client.send(client.config, metric, a, tags)
  }

  override def deriveTags(a: DoubleHistogram, tags: List[String]): List[String] = tags
}

object TotalMetric {
  def apply(metric: String): Metric[Long] = new Metric[Long] {
    override def constantTags: List[String] = List.empty

    override def sendIn(client: StatsdClient, a: Long, tags: List[String]): Unit = {
      client.send(client.config, metric, DoubleHistogram(a), tags)
    }

    override def deriveTags(a: Long, tags: List[String]): List[String] = tags
  }
}

case class TimerMetric(metric: String) extends Metric[Time] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: Time, tags: List[String]): Unit = {
    client.send(client.config, metric, a, tags)
  }

  override def deriveTags(a: Time, tags: List[String]): List[String] = tags
}

object TaggedBy {
  def apply[A](f: A => String): Metric[A] = new Metric[A] {
    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = f(a) :: tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }
}

object TaggedWith {
  def apply[A](tags: String*): Metric[A] = new Metric[A] {
    override val constantTags: List[String] = tags.toList

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }
}

object InAspect {
  def apply[A](aspect: String): Metric[A] = new Metric[A] {
    override def configure(config: StatsdConfig): StatsdConfig = config.copy(aspect = aspect)

    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }
}

object SampledAt {
  def apply[A](sampleRate: SampleRate): Metric[A] = new Metric[A] {
    override def configure(config: StatsdConfig): StatsdConfig = config.copy(sampleRate = sampleRate)

    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }
}
