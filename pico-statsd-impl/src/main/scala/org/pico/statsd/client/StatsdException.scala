package org.pico.statsd.client

class StatsdException(
    message: String,
    cause: Throwable) extends RuntimeException(message, cause)
