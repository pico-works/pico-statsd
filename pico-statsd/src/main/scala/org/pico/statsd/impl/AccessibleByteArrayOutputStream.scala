package org.pico.statsd.impl

import java.io.ByteArrayOutputStream

class AccessibleByteArrayOutputStream(capacity: Int) extends ByteArrayOutputStream(capacity) {
  def byteArray: Array[Byte] = buf

  def takeWindow(): ByteArrayWindow = {
    val window = ByteArrayWindow(buf, 0, count)
    buf = new Array[Byte](capacity)
    count = 0
    window
  }

  def drop(n: Int): Unit = {
    System.arraycopy(buf, n, buf, 0, count - n)
    count = (count - n) max 0
  }
}
