package org.pico.statsd.datapoint

trait DataPointWritable[A] {
  def write(sb: StringBuilder, prefix: String, a: A, writeExtraTags: (Boolean, StringBuilder) => Unit): Unit
}

object DataPointWritable {
  def of[D: DataPointWritable]: DataPointWritable[D] = implicitly[DataPointWritable[D]]

  implicit def singletonDataPoints[D: DataPoint: Sampling]: DataPointWritable[D] = {
    new DataPointWritable[D] {
      override def write(sb: StringBuilder, prefix: String, a: D, writeExtraTags: (Boolean, StringBuilder) => Unit): Unit = {
        DataPoint.of[D].writePrefix(sb, prefix)
        DataPoint.of[D].writeAspect(sb, a)
        sb.append(":")
        DataPoint.of[D].writeValue(sb, a)
        sb.append("|")
        DataPoint.of[D].writeType(sb)
        DataPoint.of[D].writeSampleRate(sb, a)
        writeExtraTags(DataPoint.of[D].writeTags(sb, a), sb)
      }
    }
  }
}
