package org.pico.statsd.datapoint

import java.io.PrintWriter

import org.pico.statsd.{SampleRate, TagWriter}

trait DataPointWritable[A] {
  def write(out: PrintWriter, prefix: String, aspect: String, sampleRate: SampleRate, a: A)(writeExtraTags: TagWriter => Unit): Unit
}

object DataPointWritable {
  def of[D: DataPointWritable]: DataPointWritable[D] = implicitly[DataPointWritable[D]]

  implicit def singletonDataPoints[D: DataPoint]: DataPointWritable[D] = {
    new DataPointWritable[D] {
      override def write(out: PrintWriter, prefix: String, aspect: String, sampleRate: SampleRate, a: D)(writeExtraTags: TagWriter => Unit): Unit = {
        out.print(prefix)

        if (prefix.nonEmpty && aspect.nonEmpty) {
          out.print(".")
        }

        out.print(aspect)
        out.print(":")
        DataPoint.of[D].writeValue(out, a)
        out.print("|")
        DataPoint.of[D].writeType(out)

        if (sampleRate.value != 1.0) {
          out.print("@")
          out.print(sampleRate.text)
        }

        writeExtraTags(new TagWriter(out))
      }
    }
  }
}
