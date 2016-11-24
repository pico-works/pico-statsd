package org.pico.statsd

@SerialVersionUID(3186887620964773839L)
class StatsdClientException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
}

object StatsdClientException {
  def apply(): StatsdClientException = new StatsdClientException(null, null)

  def apply(message: String): StatsdClientException = new StatsdClientException(message, null)

  def apply(message: String, cause: Throwable): StatsdClientException = new StatsdClientException(message, cause)
}