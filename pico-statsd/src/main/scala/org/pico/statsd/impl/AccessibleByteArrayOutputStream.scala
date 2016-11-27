package org.pico.statsd.impl

import java.io.ByteArrayOutputStream

class AccessibleByteArrayOutputStream(capacity: Int) extends ByteArrayOutputStream(capacity) {
  def byteArray: Array[Byte] = buf

  def drop(n: Int): Unit = {
    System.arraycopy(buf, n, buf, 0, count - n)
    count = (count - n) max 0
  }
}
