package org.pico.statsd.syntax

import org.pico.statsd.Show

package object show {
  implicit class ShowOps_4cpfCKP[A](val self: A) extends AnyVal {
    def show(implicit ev: Show[A]): String = ev.show(self)
  }

  implicit class ShowStringContext_Lj6gx7W(val sc: StringContext) extends AnyVal {
    def show[A: Show](a: A): String = sc.s(Seq(a.show))

    def show[A: Show, B: Show](a: A, b: B): String = sc.s(Seq(a.show, b.show))

    def show[A: Show, B: Show, C: Show](a: A, b: B, c: C): String = sc.s(Seq(a.show, b.show, c.show))

    def show[A: Show, B: Show, C: Show, D: Show](a: A, b: B, c: C, d: D): String = sc.s(Seq(a.show, b.show, c.show, d.show))

    def show[A: Show, B: Show, C: Show, D: Show, E: Show](a: A, b: B, c: C, d: D, e: E): String = sc.s(Seq(a.show, b.show, c.show, d.show, e.show))

    def show[A: Show, B: Show, C: Show, D: Show, E: Show, F: Show](a: A, b: B, c: C, d: D, e: E, f: F): String = sc.s(Seq(a.show, b.show, c.show, d.show, e.show, f.show))
  }
}
