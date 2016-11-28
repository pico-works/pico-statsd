package org.pico.statsd.datapoint

import org.pico.statsd.StatsdClient
import org.pico.statsd.syntax.sampler._

@specialized(Long, Double)
trait Sampler[-A] { self =>
  def constantTags: List[String]

  def deriveTags(a: A, tags: List[String]): List[String]

  def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit

  def comap[B](f: B => A): Sampler[B] = {
    new Sampler[B] {
      override def constantTags: List[String] = self.constantTags

      override def deriveTags(b: B, tags: List[String]): List[String] = self.deriveTags(f(b), tags)

      override def sendIn(client: StatsdClient, b: B, tags: List[String]): Unit = self.sendIn(client, f(b), tags)
    }
  }

  final def sendIn(client: StatsdClient, a: A): Unit = sendIn(client, a, deriveTags(a, constantTags))
}

object Sampler {
  def empty[A]: Sampler[A] = new Sampler[A] {
    override def constantTags: List[String] = List.empty

    override def deriveTags(a: A, tags: List[String]): List[String] = tags

    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }

  def of[A: Sampler]: Sampler[A] = implicitly[Sampler[A]]

  def apply[A](samplers: Sampler[A]*): Sampler[A] = (empty[A] /: samplers)(_ :+: _)

  def append[A](self: Sampler[A], that: Sampler[A]): Sampler[A] = {
    new Sampler[A] {
      override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
        self.sendIn(client, a, tags)
        that.sendIn(client, a, tags)
      }

      lazy val constantTags: List[String] = self.constantTags ++ that.constantTags

      override def deriveTags(a: A, tags: List[String]): List[String] = that.deriveTags(a, self.deriveTags(a, tags))
    }
  }
}

case class SumSampler[A](metric: String) extends Sampler[Long] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: Long, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: Long, tags: List[String]): Unit = {
    client.send(metric, Count(a), tags)
  }
}

case class CountSampler[A](metric: String) extends Sampler[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(metric, Increment(), tags)
  }
}

case class AddSampler[A](metric: String, delta: Long) extends Sampler[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(metric, Count(delta), tags)
  }
}

case class IncrementSampler[A](metric: String) extends Sampler[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(metric, Increment(), tags)
  }
}

case class DecrementSampler[A](metric: String) extends Sampler[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
    client.send(metric, Decrement(), tags)
  }
}

case class IntegralGaugeSampler(metric: String) extends Sampler[LongGauge] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: LongGauge, tags: List[String]): Unit = {
    client.send(metric, a, tags)
  }

  override def deriveTags(a: LongGauge, tags: List[String]): List[String] = tags
}

case class FractionalGaugeSampler(metric: String) extends Sampler[DoubleGauge] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: DoubleGauge, tags: List[String]): Unit = {
    client.send(metric, a, tags)
  }

  override def deriveTags(a: DoubleGauge, tags: List[String]): List[String] = tags
}

case class IntegralHistogramSampler(metric: String) extends Sampler[LongHistogram] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: LongHistogram, tags: List[String]): Unit = {
    client.send(metric, a, tags)
  }

  override def deriveTags(a: LongHistogram, tags: List[String]): List[String] = tags
}

case class FractionalHistogramSampler(metric: String) extends Sampler[DoubleHistogram] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: DoubleHistogram, tags: List[String]): Unit = {
    client.send(metric, a, tags)
  }

  override def deriveTags(a: DoubleHistogram, tags: List[String]): List[String] = tags
}

case class CounterSampler(metric: String) extends Sampler[Long] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: Long, tags: List[String]): Unit = {
    client.send(metric, DoubleHistogram(a), tags)
  }

  override def deriveTags(a: Long, tags: List[String]): List[String] = tags
}

case class TimerSampler(metric: String) extends Sampler[Time] {
  override def constantTags: List[String] = List.empty

  override def sendIn(client: StatsdClient, a: Time, tags: List[String]): Unit = {
    client.send(metric, a, tags)
  }

  override def deriveTags(a: Time, tags: List[String]): List[String] = tags
}

case class TaggedBy[A](f: A => String) extends Sampler[A] {
  override def constantTags: List[String] = List.empty

  override def deriveTags(a: A, tags: List[String]): List[String] = f(a) :: tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
}

case class TaggedWith[A](tags: List[String]) extends Sampler[A] {
  override def constantTags: List[String] = tags

  override def deriveTags(a: A, tags: List[String]): List[String] = tags

  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
}
