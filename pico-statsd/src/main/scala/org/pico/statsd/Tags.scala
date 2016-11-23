package org.pico.statsd

object Tags {
  /**
    * Generate a suffix conveying the given tag list to the client
    */
  def tagString(tags: Seq[String], tagPrefix: String): String = {
    if (tagPrefix != null) {
      if (tags == null || tags.isEmpty) {
        tagPrefix
      } else {
        val sb = new StringBuilder(tagPrefix)
        sb.append(",")
        sb.toString
      }
    } else {
      if (tags == null || tags.isEmpty) {
        ""
      } else {
        val sb = new StringBuilder("|#")
        var n: Int = tags.length - 1

        while (n >= 0) {
          sb.append(tags(n))

          if (n > 0) {
            sb.append(",")
          }

          n -= 1
          n + 1
        }

        sb.toString
      }
    }
  }
}
