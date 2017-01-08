package org.pico.statsd
import java.io.{PrintWriter, StringWriter}
import java.nio.ByteBuffer

import org.pico.disposal.Auto
import org.pico.disposal.std.autoCloseable._
import org.pico.event.{Bus, Source, View}
import org.pico.statsd.datapoint.{Alert, Metric}
import org.pico.statsd.impl.Printable

class TestStastDClient(val prefix: String, _sampleRate: SampleRate, var constantTags: Array[String] = Array.empty) extends StatsdClient {
  private val _messages = Bus[ByteBuffer]
  
  override def aspect: String = ""
  
  def messages: Source[ByteBuffer] = _messages.asSource
  
  def sampleRate: SampleRate = _sampleRate
  
  def send[D: Printable](aspect: String, metric: String, sampleRate: SampleRate, d: D, tags: Seq[String]): Unit = {
    val strWriter = new StringWriter()
    for {
      out <- Auto(new PrintWriter(strWriter))
    } {
      Printable.of[D].write(out, List(prefix, aspect).filter(_.nonEmpty).mkString("."), metric, sampleRate, d) { tagWriter =>
        constantTags.foreach(tagWriter.writeTag)
        tags.foreach(tagWriter.writeTag)
      }
    }
    
    _messages.publish(ByteBuffer.wrap(strWriter.toString.getBytes))
  }
  
  def alert[A: Alert](a: A): Unit = Alert.of[A].sendIn(this, a)
  
  def sample[A: Metric](a: A): Unit = Metric.of[A].sendIn(this, a)
  
  def sampledAt(sampleRate: SampleRate): StatsdClient =
    new ConfiguredStatsdClient(this, aspect, sampleRate)
  
  def withAspect(aspect: String): StatsdClient =
    new ConfiguredStatsdClient(this, aspect, sampleRate)
  
  def close(): Unit = ()
  
  val sentMessages: View[List[String]] =
    _messages.map(x => new String(x.array())).foldRight(List.empty[String])(_ :: _)
  
}

object TestStastDClient {
  def apply(prefix: String, sampleRate: SampleRate, tags: String*) =
    new TestStastDClient(prefix, sampleRate, tags.toArray)
}