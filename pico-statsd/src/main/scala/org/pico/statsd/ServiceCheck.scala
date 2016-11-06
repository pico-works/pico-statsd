package org.pico.statsd

case class ServiceCheck(
    name: String,
    hostname: String,
    message: String,
    checkRunId: Int,
    timestamp: Int,
    status: Status,
    tags: Array[String]) {
  def escapedMessage: String = message.replace("\n", "\\n").replace("m:", "m\\:")
}
