package org.pico.statsd.impl

import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

/** Because NumberFormat is not thread-safe we cannot share instances across threads. Use a
  * ThreadLocal to create one pre thread as this seems to offer a significant performance
  * improvement over creating one per-thread.
  */
object StatsdNumberFormat extends ThreadLocal[NumberFormat] {
  override protected def initialValue: NumberFormat = {
    // Always create the formatter for the US locale in order to avoid
    // localised non-US numbers from being written into statsd datagram packet.
    val numberFormatter = NumberFormat.getInstance(Locale.US)
    numberFormatter.setGroupingUsed(false)
    numberFormatter.setMaximumFractionDigits(6)

    // We need to specify a value for Double.NaN that is recognized by dogStatsD
    numberFormatter match {
      case decimalFormat: DecimalFormat =>
        val symbols = decimalFormat.getDecimalFormatSymbols
        symbols.setNaN("NaN")
        decimalFormat.setDecimalFormatSymbols(symbols)
      case _ =>
    }

    numberFormatter
  }
}
