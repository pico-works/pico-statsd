package org.pico.statsd.datapoint

trait DataPoints[A] {
  def write(sb: StringBuilder, prefix: String, a: A, writeExtraTags: (Boolean, StringBuilder) => Unit): Unit
}

object DataPoints {
  def of[D: DataPoints]: DataPoints[D] = implicitly[DataPoints[D]]

  implicit def singletonDataPoints[D: DataPoint: Sampling]: DataPoints[D] = {
    new DataPoints[D] {
      override def write(sb: StringBuilder, prefix: String, a: D, writeExtraTags: (Boolean, StringBuilder) => Unit): Unit = {
        DataPoint.of[D].writePrefix(sb, prefix)
        DataPoint.of[D].writeAspect(sb, a)
        sb.append(":")
        DataPoint.of[D].writeValue(sb, a)
        sb.append("|")
        DataPoint.of[D].writeType(sb)
        Sampling.of[D].writeSampleRate(sb, a)
        writeExtraTags(DataPoint.of[D].writeTags(sb, a), sb)
      }
    }
  }
}
