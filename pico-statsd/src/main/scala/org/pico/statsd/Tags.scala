package org.pico.statsd

import java.lang.{StringBuilder => JStringBuilder}

object Tags {
  /**
    * Generate a suffix conveying the given tag list to the client
    */
  def appendTagString(sb: JStringBuilder, tags: Seq[String], tagPrefix: String): Unit = {
    if (tagPrefix != null) {
      if (tags == null || tags.isEmpty) {
        sb.append(tagPrefix)
      } else {
        sb.append(tagPrefix)
        sb.append(",")
      }
    } else {
      if (tags == null || tags.isEmpty) {
        ()
      } else {
        sb.append("|#")

        var n: Int = tags.length - 1

        while (n >= 0) {
          sb.append(tags(n))

          if (n > 0) {
            sb.append(",")
          }

          n -= 1
          n + 1
        }
      }
    }
  }
}
