package org.pico.statsd.client

sealed trait Priority

case object Low extends Priority
case object Normal extends Priority
