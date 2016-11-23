package org.pico.statsd

import java.text.DecimalFormat

case class SampleRate(value: Double) {
  lazy val text: String = SampleRate.decimalFormat.format(value)
}

object SampleRate {
  val always = SampleRate(1)

  val never = SampleRate(0)

  private val decimalFormat = new DecimalFormat("#.################")
}
