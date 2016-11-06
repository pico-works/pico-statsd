package org.pico

package object statsd {
  val Ok = Status(0)
  val Warning = Status(1)
  val Critical = Status(2)
  val Unknown = Status(3)
}
