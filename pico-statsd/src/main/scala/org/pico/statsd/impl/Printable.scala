package org.pico.statsd.impl

import java.io.PrintWriter

import org.pico.statsd.SampleRate
import org.pico.statsd.datapoint.DataPoint

trait Printable[A] {
  def write(out: PrintWriter, prefix: String, metric: String, sampleRate: SampleRate, a: A)(writeExtraTags: TagWriter => Unit): Unit
}

object Printable {
  def of[D: Printable]: Printable[D] = implicitly[Printable[D]]

  implicit def singletonDataPoints[D: DataPoint]: Printable[D] = {
    new Printable[D] {
      override def write(out: PrintWriter, prefix: String, metric: String, sampleRate: SampleRate, a: D)(writeExtraTags: TagWriter => Unit): Unit = {
        out.print(prefix)

        if (prefix.nonEmpty && metric.nonEmpty) {
          out.print(".")
        }

        out.print(metric)
        out.print(":")
        DataPoint.of[D].writeValue(out, a)
        out.print("|")
        DataPoint.of[D].writeType(out)

        if (sampleRate.value != 1.0) {
          out.print("|@")
          out.print(sampleRate.text)
        }

        writeExtraTags(new TagWriter(out))
      }
    }
  }
}
