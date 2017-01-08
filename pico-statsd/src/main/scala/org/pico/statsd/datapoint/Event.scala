package org.pico.statsd.datapoint

import org.pico.statsd.StatsdClient
import org.pico.statsd.syntax.sampler._

object Priority extends Enumeration {
  val Normal, Low = Value
}

object AlertType extends Enumeration {
  val Error, Warning, Info, Success = Value
}

case class EventData(text: String, priority: Priority.Value, alertType: AlertType.Value)

trait Alert[-A] { self =>
  def constantTags: List[String]
  
  def deriveTags(a: A, tags: List[String]): List[String]
  
  def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit
  
  def comap[B](f: B => A): Alert[B] = {
    new Alert[B] {
      override def constantTags: List[String] = self.constantTags
      
      override def deriveTags(b: B, tags: List[String]): List[String] = self.deriveTags(f(b), tags)
      
      override def sendIn(client: StatsdClient, b: B, tags: List[String]): Unit = self.sendIn(client, f(b), tags)
    }
  }
  
  final def sendIn(client: StatsdClient, a: A): Unit = sendIn(client, a, deriveTags(a, constantTags))
}

object Alert {
  def empty[A]: Alert[A] = new Alert[A] {
    override def constantTags: List[String] = List.empty
    override def deriveTags(a: A, tags: List[String]): List[String] = tags
    override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
  }
  
  def of[A: Alert]: Alert[A] = implicitly[Alert[A]]
  
  def apply[A](events: Alert[A]*): Alert[A] = (empty[A] /: events) (_ :+: _)
  
  def append[A](self: Alert[A], that: Alert[A]): Alert[A] = {
    new Alert[A] {
      override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = {
        self.sendIn(client, a, tags)
        that.sendIn(client, a, tags)
      }
      
      lazy val constantTags: List[String] = self.constantTags ++ that.constantTags
      
      override def deriveTags(a: A, tags: List[String]): List[String] = that.deriveTags(a, self.deriveTags(a, tags))
    }
  }
}

case class Event(title: String) extends Alert[EventData] {
  def constantTags: List[String] = List.empty
  
  def deriveTags(a: EventData, tags: List[String]): List[String] = tags
  
  def sendIn(client: StatsdClient, a: EventData, tags: List[String]): Unit = {
    client.send(title, a, tags)
  }
}

case class EventTaggedBy[A](f: A => String) extends Alert[A] {
  override def constantTags: List[String] = List.empty
  
  override def deriveTags(a: A, tags: List[String]): List[String] = f(a) :: tags
  
  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
}

case class EventTaggedWith[A](tags: List[String]) extends Alert[A] {
  override def constantTags: List[String] = tags
  
  override def deriveTags(a: A, tags: List[String]): List[String] = tags
  
  override def sendIn(client: StatsdClient, a: A, tags: List[String]): Unit = ()
}