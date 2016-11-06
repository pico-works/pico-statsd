package org.pico.statsd

import java.text.{DecimalFormat, DecimalFormatSymbols, NumberFormat}
import java.util.Locale

object NumberFormatters extends ThreadLocal[NumberFormat] {
  override protected def initialValue: NumberFormat = {
    // Always create the formatter for the US locale in order to avoid this bug:
    // https://github.com/indeedeng/java-dogstatsd-client/issues/3
    val numberFormatter: NumberFormat = NumberFormat.getInstance(Locale.US)
    numberFormatter.setGroupingUsed(false)
    numberFormatter.setMaximumFractionDigits(6)

    // we need to specify a value for Double.NaN that is recognized by dogStatsD
    numberFormatter match {
      case decimalFormat: DecimalFormat =>
        val symbols: DecimalFormatSymbols = decimalFormat.getDecimalFormatSymbols
        symbols.setNaN("NaN")
        decimalFormat.setDecimalFormatSymbols(symbols)
      case _ =>
    }

    numberFormatter
  }
}
