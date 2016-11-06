package org.pico.statsd.client

import java.util.concurrent.{Executors, ThreadFactory}

object StatsdDaemonThreadFactory extends ThreadFactory {
  val delegate: ThreadFactory = Executors.defaultThreadFactory

  override def newThread(r: Runnable): Thread = {
    val result: Thread = delegate.newThread(r)
    result.setName("StatsD-" + result.getName)
    result.setDaemon(true)
    result
  }
}
