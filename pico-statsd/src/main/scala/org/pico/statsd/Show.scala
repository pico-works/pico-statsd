package org.pico.statsd

trait Show[A] {
  def show(value: A): String
}

object Show {
  def apply[A](f: A => String): Show[A] = {
    new Show[A] {
      override def show(value: A): String = f(value)
    }
  }

  implicit val show_String_QUhNwZM = Show[String](_.toString)

  implicit val show_Long_DkNqh3g = Show[Long](_.toString)

  implicit val show_Double_Zfybm4J = Show[Double](_.toString)

  implicit val show_Int_eTAbMX4 = Show[Int](_.toString)
}
