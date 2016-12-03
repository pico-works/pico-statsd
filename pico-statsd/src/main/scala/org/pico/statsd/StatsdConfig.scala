package org.pico.statsd

/** Configuration for [[StatsdClient]] or [[Metric]]].
  *
  * @param aspect The aspect of the metric.  No aspect is represented as an empty string.
  * @param sampleRate The sample rate as a probability
  */
case class StatsdConfig(
    aspect: String,
    sampleRate: SampleRate)

object StatsdConfig {
  val default = StatsdConfig("", SampleRate.always)
}
