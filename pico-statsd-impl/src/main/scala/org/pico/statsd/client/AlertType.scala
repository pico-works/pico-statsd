package org.pico.statsd.client

sealed trait AlertType

case object Error extends AlertType
case object Warn extends AlertType
case object Info extends AlertType
case object Success extends AlertType
