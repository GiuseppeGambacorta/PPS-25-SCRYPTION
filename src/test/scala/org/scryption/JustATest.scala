package org.scryption

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class JustATest extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks:

  "L'ambiente di test" should "verificare le operazioni base" in:
    (1 + 1) shouldBe 2

  "Il contatore" should "incrementare il valore di uno" in:
    Main.incrementCounter(0) shouldBe 1
    Main.incrementCounter(41) shouldBe 42

  it should "funzionare per qualsiasi intero (Property-Based Test con ScalaCheck)" in:
    forAll { (n: Int) =>
      whenever(n < Int.MaxValue) {
        Main.incrementCounter(n) shouldBe n + 1
      }
    }
