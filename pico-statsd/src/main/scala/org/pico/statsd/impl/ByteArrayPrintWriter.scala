package org.pico.statsd.impl

import java.io.PrintWriter

class ByteArrayPrintWriter(val outputStream: AccessibleByteArrayOutputStream) extends PrintWriter(outputStream, true) {
  def this(capacity: Int) {
    this(new AccessibleByteArrayOutputStream(capacity))
  }
}
