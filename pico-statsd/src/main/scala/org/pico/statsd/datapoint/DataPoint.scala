package org.pico.statsd.datapoint

import java.io.PrintWriter

import org.pico.statsd.impl.StatsdNumberFormat

trait DataPoint[A] {
  def writeSampleRate(out: PrintWriter, a: A): Unit

  def writeValue(out: PrintWriter, a: A): Unit

  def writeType(out: PrintWriter): Unit
}

object DataPoint {
  def of[D: DataPoint]: DataPoint[D] = implicitly[DataPoint[D]]
}

case class Count(delta: Long) extends AnyVal

object Count {
  implicit val dataPoint_Count = new DataPoint[Count] {
    override def writeValue(out: PrintWriter, a: Count): Unit = out.print(a.delta)

    override def writeType(out: PrintWriter): Unit = out.print("c")

    override def writeSampleRate(out: PrintWriter, a: Count): Unit = ()
  }
}

case class Increment()

object Increment {
  implicit val dataPoint_Increment = new DataPoint[Increment] {
    override def writeValue(out: PrintWriter, a: Increment): Unit = out.print(1L)

    override def writeType(out: PrintWriter): Unit = out.print("c")

    override def writeSampleRate(out: PrintWriter, a: Increment): Unit = ()
  }
}

case class Decrement()

object Decrement {
  implicit val dataPoint_Decrement = new DataPoint[Decrement] {
    override def writeValue(out: PrintWriter, a: Decrement): Unit = out.print(-1L)

    override def writeType(out: PrintWriter): Unit = out.print("c")

    override def writeSampleRate(out: PrintWriter, a: Decrement): Unit = ()
  }
}

case class DoubleGauge(value: Double) extends AnyVal

object DoubleGauge {
  implicit val dataPoint_DoubleGauge = new DataPoint[DoubleGauge] {
    override def writeValue(out: PrintWriter, a: DoubleGauge): Unit = out.print(StatsdNumberFormat.get.format(a.value))

    override def writeType(out: PrintWriter): Unit = out.print("g")

    override def writeSampleRate(out: PrintWriter, a: DoubleGauge): Unit = ()
  }
}

case class LongGauge(value: Long) extends AnyVal

object LongGauge {
  implicit val dataPoint_LongGauge = new DataPoint[LongGauge] {
    override def writeValue(out: PrintWriter, a: LongGauge): Unit = out.print(a.value)

    override def writeType(out: PrintWriter): Unit = out.print("g")

    override def writeSampleRate(out: PrintWriter, a: LongGauge): Unit = ()
  }
}

case class Time(timeInMs: Long) extends AnyVal

object Time {
  implicit val dataPoint_Time = new DataPoint[Time] {
    override def writeValue(out: PrintWriter, a: Time): Unit = out.print(a.timeInMs)

    override def writeType(out: PrintWriter): Unit = out.print("ms")

    override def writeSampleRate(out: PrintWriter, a: Time): Unit = ()
  }
}

case class DoubleHistogram(value: Double) extends AnyVal

object DoubleHistogram {
  implicit val dataPoint_DoubleHistogram = new DataPoint[DoubleHistogram] {
    override def writeValue(out: PrintWriter, a: DoubleHistogram): Unit = out.print(StatsdNumberFormat.get.format(a.value))

    override def writeType(out: PrintWriter): Unit = out.print("h")

    override def writeSampleRate(out: PrintWriter, a: DoubleHistogram): Unit = ()
  }
}

case class LongHistogram(value: Long) extends AnyVal

object LongHistogram {
  implicit val dataPoint_LongHistogram = new DataPoint[LongHistogram] {
    override def writeValue(out: PrintWriter, a: LongHistogram): Unit = out.print(a.value)

    override def writeType(out: PrintWriter): Unit = out.print("h")

    override def writeSampleRate(out: PrintWriter, a: LongHistogram): Unit = ()
  }
}
