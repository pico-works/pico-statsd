package org.pico.statsd

import org.pico.statsd.datapoint.{AlertType, EventData, Priority}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

package object arb {
  case class Identifier(value: String) extends AnyVal
  
  implicit val arbitraryPriority: Arbitrary[Priority.Value] = Arbitrary(Gen.oneOf(Priority.values.toSeq))
  implicit val arbitraryAlert: Arbitrary[AlertType.Value] = Arbitrary(Gen.oneOf(AlertType.values.toSeq))
  
  implicit val arbitraryEventData: Arbitrary[EventData] = Arbitrary(for {
    txt <- arbitrary[String]
    pri <- arbitrary[Priority.Value]
    alt <- arbitrary[AlertType.Value]
  } yield EventData(txt, pri, alt))
  
  implicit val arbitraryId = Arbitrary(Gen.identifier.map(Identifier))
  
}
