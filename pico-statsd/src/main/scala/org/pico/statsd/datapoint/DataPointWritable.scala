package org.pico.statsd.datapoint

import org.pico.statsd.TagWriter

trait DataPointWritable[A] {
  def write(sb: StringBuilder, prefix: String, aspect: String, a: A)(writeExtraTags: TagWriter => Unit): Unit
}

object DataPointWritable {
  def of[D: DataPointWritable]: DataPointWritable[D] = implicitly[DataPointWritable[D]]

  implicit def singletonDataPoints[D: DataPoint: Sampling]: DataPointWritable[D] = {
    new DataPointWritable[D] {
      override def write(sb: StringBuilder, prefix: String, aspect: String, a: D)(writeExtraTags: TagWriter => Unit): Unit = {
        sb.append(prefix)

        if (prefix.nonEmpty && aspect.nonEmpty) {
          sb.append(".")
        }

        sb.append(aspect)
        sb.append(":")
        DataPoint.of[D].writeValue(sb, a)
        sb.append("|")
        DataPoint.of[D].writeType(sb)
        DataPoint.of[D].writeSampleRate(sb, a)
        writeExtraTags(new TagWriter(sb))
      }
    }
  }
}
