package org.pico.statsd.datapoint

import org.pico.statsd.{SampleRate, TagWriter}

trait DataPointWritable[A] {
  def write(sb: StringBuilder, prefix: String, aspect: String, sampleRate: SampleRate, a: A)(writeExtraTags: TagWriter => Unit): Unit
}

object DataPointWritable {
  def of[D: DataPointWritable]: DataPointWritable[D] = implicitly[DataPointWritable[D]]

  implicit def singletonDataPoints[D: DataPoint]: DataPointWritable[D] = {
    new DataPointWritable[D] {
      override def write(sb: StringBuilder, prefix: String, aspect: String, sampleRate: SampleRate, a: D)(writeExtraTags: TagWriter => Unit): Unit = {
        sb.append(prefix)

        if (prefix.nonEmpty && aspect.nonEmpty) {
          sb.append(".")
        }

        sb.append(aspect)
        sb.append(":")
        DataPoint.of[D].writeValue(sb, a)
        sb.append("|")
        DataPoint.of[D].writeType(sb)

        if (sampleRate.value != 1.0) {
          sb.append("@")
          sb.append(sampleRate.text)
        }

        writeExtraTags(new TagWriter(sb))
      }
    }
  }
}
