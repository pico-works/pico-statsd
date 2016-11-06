package org.pico.statsd

trait StatsdErrorHandler {
  def handle(exception: Exception): Unit
}
