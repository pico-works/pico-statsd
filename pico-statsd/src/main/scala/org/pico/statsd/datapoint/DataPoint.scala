package org.pico.statsd.datapoint

import org.pico.statsd.{SampleRate, StatsdNumberFormat}

trait DataPoint[A] {
  def writeSampleRate(sb: StringBuilder, a: A): Unit

  def writeValue(sb: StringBuilder, a: A): Unit

  def writeType(sb: StringBuilder): Unit

  def writeTags(sb: StringBuilder, a: A): Boolean
}

object DataPoint {
  def of[D: DataPoint]: DataPoint[D] = implicitly[DataPoint[D]]
}

case class Count(aspect: String, delta: Long, tags: Seq[String])

object Count {
  implicit val dataPoint_Count = new DataPoint[Count] with Sampling[Count] {
    override def sampleRate(a: Count): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: Count): Unit = sb.append(a.delta)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Count): Unit = ()

    override def writeTags(sb: StringBuilder, a: Count): Boolean = false
  }
}

case class IncrementCounter(tags: Seq[String])

object IncrementCounter {
  implicit val dataPoint_IncrementCounter = new DataPoint[IncrementCounter] with Sampling[IncrementCounter] {
    override def sampleRate(a: IncrementCounter): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: IncrementCounter): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: IncrementCounter): Unit = ()

    override def writeTags(sb: StringBuilder, a: IncrementCounter): Boolean = false
  }
}

case class Increment(tags: Seq[String])

object Increment {
  implicit val dataPoint_Increment = new DataPoint[Increment] with Sampling[Increment] {
    override def sampleRate(a: Increment): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: Increment): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Increment): Unit = ()

    override def writeTags(sb: StringBuilder, a: Increment): Boolean = false
  }
}

case class DecrementCounter(tags: Seq[String])

object DecrementCounter {
  implicit val dataPoint_DecrementCounter = new DataPoint[DecrementCounter] with Sampling[DecrementCounter] {
    override def sampleRate(a: DecrementCounter): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: DecrementCounter): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: DecrementCounter): Unit = ()

    override def writeTags(sb: StringBuilder, a: DecrementCounter): Boolean = false
  }
}

case class Decrement(tags: Seq[String])

object Decrement {
  implicit val dataPoint_Decrement = new DataPoint[Decrement] with Sampling[Decrement] {
    override def sampleRate(a: Decrement): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: Decrement): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Decrement): Unit = ()

    override def writeTags(sb: StringBuilder, a: Decrement): Boolean = false
  }
}

case class DoubleGauge(value: Double, tags: Seq[String])

object DoubleGauge {
  implicit val dataPoint_DoubleGauge = new DataPoint[DoubleGauge] with Sampling[DoubleGauge] {
    override def sampleRate(a: DoubleGauge): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: DoubleGauge): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder, a: DoubleGauge): Unit = ()

    override def writeTags(sb: StringBuilder, a: DoubleGauge): Boolean = false
  }
}

case class LongGauge(value: Long, tags: Seq[String])

object LongGauge {
  implicit val dataPoint_LongGauge = new DataPoint[LongGauge] with Sampling[LongGauge] {
    override def sampleRate(a: LongGauge): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: LongGauge): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder, a: LongGauge): Unit = ()

    override def writeTags(sb: StringBuilder, a: LongGauge): Boolean = false
  }
}

case class Time(aspect: String, timeInMs: Long, tags: Seq[String])

object Time {
  implicit val dataPoint_Time = new DataPoint[Time] with Sampling[Time] {
    override def sampleRate(a: Time): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: Time): Unit = sb.append(a.timeInMs)

    override def writeType(sb: StringBuilder): Unit = sb.append("ms")

    override def writeSampleRate(sb: StringBuilder, a: Time): Unit = ()

    override def writeTags(sb: StringBuilder, a: Time): Boolean = false
  }
}

case class DoubleHistogram(value: Double, tags: Seq[String])

object DoubleHistogram {
  implicit val dataPoint_DoubleHistogram = new DataPoint[DoubleHistogram] with Sampling[DoubleHistogram] {
    override def sampleRate(a: DoubleHistogram): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: DoubleHistogram): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder, a: DoubleHistogram): Unit = ()

    override def writeTags(sb: StringBuilder, a: DoubleHistogram): Boolean = false
  }
}

case class LongHistogram(value: Long, tags: Seq[String])

object LongHistogram {
  implicit val dataPoint_LongHistogram = new DataPoint[LongHistogram] with Sampling[LongHistogram] {
    override def sampleRate(a: LongHistogram): SampleRate = SampleRate.always

    override def writeValue(sb: StringBuilder, a: LongHistogram): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder, a: LongHistogram): Unit = ()

    override def writeTags(sb: StringBuilder, a: LongHistogram): Boolean = false
  }
}

case class SampleRated[A](sampleRate: SampleRate, value: A)

object SampleRated {
  implicit def dataPoint_SampleRate[A: DataPoint] = new DataPoint[SampleRated[A]] with Sampling[SampleRated[A]] {
    override def sampleRate(a: SampleRated[A]): SampleRate = a.sampleRate

    override def writeValue(sb: StringBuilder, a: SampleRated[A]): Unit = DataPoint.of[A].writeValue(sb, a.value)

    override def writeType(sb: StringBuilder): Unit = DataPoint.of[A].writeType(sb)

    override def writeSampleRate(sb: StringBuilder, a: SampleRated[A]): Unit = sb.append(a.sampleRate)

    override def writeTags(sb: StringBuilder, a: SampleRated[A]): Boolean = DataPoint.of[A].writeTags(sb, a.value)
  }
}

case class DataPointContext[A](aspect: String, sampleRate: SampleRate, value: A, tags: String*)

object DataPointContext {
  implicit def dataPoints_DataPointContext[A: DataPoint] = new DataPoint[DataPointContext[A]] with Sampling[DataPointContext[A]] {
    override def sampleRate(a: DataPointContext[A]): SampleRate = a.sampleRate

    override def writeValue(sb: StringBuilder, a: DataPointContext[A]): Unit = DataPoint.of[A].writeValue(sb, a.value)

    override def writeType(sb: StringBuilder): Unit = DataPoint.of[A].writeType(sb)

    override def writeSampleRate(sb: StringBuilder, a: DataPointContext[A]): Unit = sb.append(a.sampleRate)

    override def writeTags(sb: StringBuilder, a: DataPointContext[A]): Boolean = {
      var written = DataPoint.of[A].writeTags(sb, a.value)

      a.tags.foreach { tag =>
        if (written) {
          sb.append(",")
        }

        sb.append(tag)

        written = true
      }

      written
    }
  }
}
