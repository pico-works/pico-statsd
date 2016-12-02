package org.pico.statsd

case class StatsdConfig(
    aspect: String,
    sampleRate: SampleRate)

object StatsdConfig {
  val defaultConfig = StatsdConfig("", SampleRate.always)
}
