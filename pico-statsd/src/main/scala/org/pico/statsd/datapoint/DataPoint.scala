package org.pico.statsd.datapoint

import org.pico.statsd.{SampleRate, StatsdNumberFormat}

trait DataPoint[A] {
  def sampleRate(a: A): SampleRate

  def writePrefix(sb: StringBuilder, prefix: String): Unit

  def writeAspect(sb: StringBuilder, a: A): Unit

  def writeValue(sb: StringBuilder, a: A): Unit

  def writeType(sb: StringBuilder): Unit

  def writeSampleRate(sb: StringBuilder): Unit

  def writeTags(sb: StringBuilder, a: A): Boolean
}

object DataPoint {
  def of[D: DataPoint]: DataPoint[D] = implicitly[DataPoint[D]]
}

case class Count(aspect: String, delta: Long, tags: Seq[String])

object Count {
  implicit val dataPoint_Count = new DataPoint[Count] with Sampling[Count] {
    override def sampleRate(a: Count): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: Count): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: Count): Unit = sb.append(a.delta)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: Count): Boolean = false
  }
}

case class IncrementCounter(aspect: String, tags: String*)

object IncrementCounter {
  implicit val dataPoint_IncrementCounter = new DataPoint[IncrementCounter] {
    override def sampleRate(a: IncrementCounter): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: IncrementCounter): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: IncrementCounter): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: IncrementCounter): Boolean = false
  }
}

case class Increment(aspect: String, tags: String*)

object Increment {
  implicit val dataPoint_Increment = new DataPoint[Increment] {
    override def sampleRate(a: Increment): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: Increment): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: Increment): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: Increment): Boolean = false
  }
}

case class DecrementCounter(aspect: String, tags: String*)

object DecrementCounter {
  implicit val dataPoint_DecrementCounter = new DataPoint[DecrementCounter] {
    override def sampleRate(a: DecrementCounter): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: DecrementCounter): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: DecrementCounter): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: DecrementCounter): Boolean = false
  }
}

case class Decrement(aspect: String, tags: String*)

object Decrement {
  implicit val dataPoint_Decrement = new DataPoint[Decrement] {
    override def sampleRate(a: Decrement): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: Decrement): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: Decrement): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: Decrement): Boolean = false
  }
}

case class DoubleGauge(aspect: String, value: Double, tags: String*)

object DoubleGauge {
  implicit val dataPoint_DoubleGauge = new DataPoint[DoubleGauge] {
    override def sampleRate(a: DoubleGauge): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: DoubleGauge): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: DoubleGauge): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: DoubleGauge): Boolean = false
  }
}

case class LongGauge(aspect: String, value: Long, tags: String*)

object LongGauge {
  implicit val dataPoint_LongGauge = new DataPoint[LongGauge] {
    override def sampleRate(a: LongGauge): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: LongGauge): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: LongGauge): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: LongGauge): Boolean = false
  }
}

case class Time(aspect: String, timeInMs: Long, tags: String*)

object Time {
  implicit val dataPoint_Time = new DataPoint[Time] {
    override def sampleRate(a: Time): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: Time): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: Time): Unit = sb.append(a.timeInMs)

    override def writeType(sb: StringBuilder): Unit = sb.append("ms")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: Time): Boolean = false
  }
}

case class DoubleHistogram(aspect: String, value: Double, tags: String*)

object DoubleHistogram {
  implicit val dataPoint_DoubleHistogram = new DataPoint[DoubleHistogram] {
    override def sampleRate(a: DoubleHistogram): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: DoubleHistogram): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: DoubleHistogram): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: DoubleHistogram): Boolean = false
  }
}

case class LongHistogram(aspect: String, value: Long, tags: String*)

object LongHistogram {
  implicit val dataPoint_LongHistogram = new DataPoint[LongHistogram] {
    override def sampleRate(a: LongHistogram): SampleRate = SampleRate.always

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = sb.append(prefix)

    override def writeAspect(sb: StringBuilder, a: LongHistogram): Unit = sb.append(a.aspect)

    override def writeValue(sb: StringBuilder, a: LongHistogram): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder): Unit = ()

    override def writeTags(sb: StringBuilder, a: LongHistogram): Boolean = false
  }
}

case class Sampled[A](sampleRate: SampleRate, value: A)

object Sampled {
  implicit def dataPoint_SampleRate[A: DataPoint] = new DataPoint[Sampled[A]] with Sampling[Sampled[A]] {
    override def sampleRate(a: Sampled[A]): SampleRate = a.sampleRate

    override def writePrefix(sb: StringBuilder, prefix: String): Unit = DataPoint.of[A].writePrefix(sb, prefix)

    override def writeAspect(sb: StringBuilder, a: Sampled[A]): Unit = DataPoint.of[A].writeAspect(sb, a.value)

    override def writeValue(sb: StringBuilder, a: Sampled[A]): Unit = DataPoint.of[A].writeValue(sb, a.value)

    override def writeType(sb: StringBuilder): Unit = DataPoint.of[A].writeType(sb)

    override def writeSampleRate(sb: StringBuilder): Unit = DataPoint.of[A].writeSampleRate(sb)

    override def writeTags(sb: StringBuilder, a: Sampled[A]): Boolean = DataPoint.of[A].writeTags(sb, a.value)
  }
}
