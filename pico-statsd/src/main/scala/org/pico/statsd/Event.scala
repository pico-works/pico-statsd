package org.pico.statsd

case class Event(
    title: String,
    text: String,
    millisSinceEpoch: Long,
    hostname: String,
    aggregationKey: String,
    priority: String ,
    sourceTypeName: String,
    alertType: String)
