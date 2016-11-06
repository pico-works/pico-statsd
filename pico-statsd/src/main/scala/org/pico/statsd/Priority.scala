package org.pico.statsd

sealed trait Priority

case object Low extends Priority
case object Normal extends Priority
