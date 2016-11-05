package org.pico.statsd.client

trait StatsdErrorHandler {
  def handle(exception: Exception): Unit
}
