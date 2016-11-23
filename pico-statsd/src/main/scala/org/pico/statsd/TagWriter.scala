package org.pico.statsd

class TagWriter(sb: StringBuilder) {
  var written: Boolean = false

  def writeTag(tag: String): Unit = {
    if (written) {
      sb.append(",")
    } else {
      sb.append("|#")
    }

    sb.append(tag)

    written = true
  }
}
