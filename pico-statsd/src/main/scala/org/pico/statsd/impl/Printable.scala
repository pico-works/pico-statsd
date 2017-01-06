package org.pico.statsd.impl

import java.io.PrintWriter

import org.pico.statsd.SampleRate
import org.pico.statsd.datapoint._

trait Printable[A] {
  def write(out: PrintWriter, prefix: String, metric: String, sampleRate: SampleRate, a: A)(writeExtraTags: TagWriter => Unit): Unit
}

object Printable {
  def of[D: Printable]: Printable[D] = implicitly[Printable[D]]

  implicit def singletonDataPoints[D: DataPoint]: Printable[D] = {
    new Printable[D] {
      override def write(out: PrintWriter, prefix: String, metric: String, sampleRate: SampleRate, a: D)(writeExtraTags: TagWriter => Unit): Unit = {
        out.print(prefix)

        if (prefix.nonEmpty && metric.nonEmpty) {
          out.print(".")
        }

        out.print(metric)
        out.print(":")
        DataPoint.of[D].writeValue(out, a)
        out.print("|")
        DataPoint.of[D].writeType(out)

        if (sampleRate.value != 1.0) {
          out.print("|@")
          out.print(sampleRate.text)
        }

        writeExtraTags(new TagWriter(out))
      }
    }
  }
  
  implicit val datadogEventIsPrintable: Printable[EventData] = {
    @inline
    def priority(p: Priority.Value): String = p match {
      case Priority.Low    => "|p:low"
      case Priority.Normal => "|p:normal"
    }
    
    @inline
    def alertType(a: AlertType.Value): String = a match {
      case AlertType.Error   => "|t:error"
      case AlertType.Warning => "|t:warning"
      case AlertType.Info    => "|t:info"
      case AlertType.Success => "|t:success"
    }
    new Printable[EventData] {
      def write(out: PrintWriter, prefix: String, metric: String, sampleRate: SampleRate, a: EventData)(writeExtraTags: (TagWriter) => Unit): Unit = {
        val titleLen = prefix.length + metric.length + (if (prefix.nonEmpty && metric.nonEmpty) 1 else 0)
        out.print("_e{")
        out.print(titleLen)
        out.print(",")
        out.print(a.text.length)
        out.print("}:")
        
        out.print(prefix)
        if (prefix.nonEmpty && metric.nonEmpty) {
          out.print(".")
        }
        out.print(metric)
        out.print("|")
        out.print(a.text)
        out.print(priority(a.priority))
        out.print(alertType(a.alertType))
  
        writeExtraTags(new TagWriter(out))
      }
    }
  }
}
