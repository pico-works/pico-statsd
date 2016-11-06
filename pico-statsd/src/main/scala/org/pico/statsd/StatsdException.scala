package org.pico.statsd

class StatsdException(
    message: String,
    cause: Throwable) extends RuntimeException(message, cause)
