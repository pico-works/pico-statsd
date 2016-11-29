package org.pico.statsd.syntax

import org.pico.statsd.datapoint.{Metric, TaggedBy, TaggedWith}

package object metric {
  implicit class MetricOps_vy4ffYh[A](val self: Metric[A]) extends AnyVal {
    final def :+:(that: Metric[A]): Metric[A] = Metric.append(self, that)

    final def tagged(tags: Seq[String]): Metric[A] = self :+: TaggedWith[A](tags)

    final def taggedBy(f: A => String): Metric[A] = self :+: TaggedBy(f)
  }
}
