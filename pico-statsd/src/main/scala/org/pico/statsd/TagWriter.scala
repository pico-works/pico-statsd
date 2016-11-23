package org.pico.statsd

import java.io.PrintWriter

class TagWriter(out: PrintWriter) {
  var written: Boolean = false

  def writeTag(tag: String): Unit = {
    if (written) {
      out.print(",")
    } else {
      out.print("|#")
    }

    out.print(tag)

    written = true
  }
}
