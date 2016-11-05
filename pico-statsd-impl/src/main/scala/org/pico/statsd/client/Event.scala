package org.pico.statsd.client

case class Event(
    title: String,
    text: String,
    millisSinceEpoch: Long,
    hostname: String,
    aggregationKey: String,
    priority: String ,
    sourceTypeName: String,
    alertType: String)
