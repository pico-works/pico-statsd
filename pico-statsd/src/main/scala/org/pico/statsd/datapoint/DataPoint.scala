package org.pico.statsd.datapoint

import org.pico.statsd.{SampleRate, StatsdNumberFormat}

trait DataPoint[A] {
  def writeSampleRate(sb: StringBuilder, a: A): Unit

  def writeValue(sb: StringBuilder, a: A): Unit

  def writeType(sb: StringBuilder): Unit
}

object DataPoint {
  def of[D: DataPoint]: DataPoint[D] = implicitly[DataPoint[D]]
}

case class Count(delta: Long)

object Count {
  implicit val dataPoint_Count = new DataPoint[Count] {
    override def writeValue(sb: StringBuilder, a: Count): Unit = sb.append(a.delta)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Count): Unit = ()
  }
}

case class IncrementCounter(tags: Seq[String])

object IncrementCounter {
  implicit val dataPoint_IncrementCounter = new DataPoint[IncrementCounter] {
    override def writeValue(sb: StringBuilder, a: IncrementCounter): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: IncrementCounter): Unit = ()
  }
}

case class Increment(tags: Seq[String])

object Increment {
  implicit val dataPoint_Increment = new DataPoint[Increment] {
    override def writeValue(sb: StringBuilder, a: Increment): Unit = sb.append(1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Increment): Unit = ()
  }
}

case class DecrementCounter(tags: Seq[String])

object DecrementCounter {
  implicit val dataPoint_DecrementCounter = new DataPoint[DecrementCounter] {
    override def writeValue(sb: StringBuilder, a: DecrementCounter): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: DecrementCounter): Unit = ()
  }
}

case class Decrement(tags: Seq[String])

object Decrement {
  implicit val dataPoint_Decrement = new DataPoint[Decrement] {
    override def writeValue(sb: StringBuilder, a: Decrement): Unit = sb.append(-1L)

    override def writeType(sb: StringBuilder): Unit = sb.append("c")

    override def writeSampleRate(sb: StringBuilder, a: Decrement): Unit = ()
  }
}

case class DoubleGauge(value: Double)

object DoubleGauge {
  implicit val dataPoint_DoubleGauge = new DataPoint[DoubleGauge] {
    override def writeValue(sb: StringBuilder, a: DoubleGauge): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder, a: DoubleGauge): Unit = ()
  }
}

case class LongGauge(value: Long)

object LongGauge {
  implicit val dataPoint_LongGauge = new DataPoint[LongGauge] {
    override def writeValue(sb: StringBuilder, a: LongGauge): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("g")

    override def writeSampleRate(sb: StringBuilder, a: LongGauge): Unit = ()
  }
}

case class Time(timeInMs: Long)

object Time {
  implicit val dataPoint_Time = new DataPoint[Time] {
    override def writeValue(sb: StringBuilder, a: Time): Unit = sb.append(a.timeInMs)

    override def writeType(sb: StringBuilder): Unit = sb.append("ms")

    override def writeSampleRate(sb: StringBuilder, a: Time): Unit = ()
  }
}

case class DoubleHistogram(value: Double)

object DoubleHistogram {
  implicit val dataPoint_DoubleHistogram = new DataPoint[DoubleHistogram] {
    override def writeValue(sb: StringBuilder, a: DoubleHistogram): Unit = sb.append(StatsdNumberFormat.get.format(a.value))

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder, a: DoubleHistogram): Unit = ()
  }
}

case class LongHistogram(value: Long)

object LongHistogram {
  implicit val dataPoint_LongHistogram = new DataPoint[LongHistogram] {
    override def writeValue(sb: StringBuilder, a: LongHistogram): Unit = sb.append(a.value)

    override def writeType(sb: StringBuilder): Unit = sb.append("h")

    override def writeSampleRate(sb: StringBuilder, a: LongHistogram): Unit = ()
  }
}
