package org.scryption

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

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


class FeatureSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  val sumNumbers = (a: Int, b: Int) => a + b

  Feature("Somma di numeri interi") {

    Scenario("Somma di due numeri positivi specifici") {
      Given("Due numeri positivi 5 e 10")
      val a = 5
      val b = 10

      When("Vengono sommati insieme")
      val result = sumNumbers(a, b)

      Then("Il risultato deve essere 15")
      result shouldBe 15
    }

    Scenario("Elemento neutro dell'addizione (zero)") {
      Given("Un numero qualsiasi e lo zero")
      val a = 42
      val zero = 0

      When("Lo zero viene sommato al numero")
      val result = sumNumbers(a, zero)

      Then("Il risultato deve essere identico al numero iniziale")
      result shouldBe a
    }

    Scenario("Proprietà commutativa su qualsiasi coppia di interi (Property-Based)") {
      Given("La funzione sumNumbers")

      When("Viene eseguita su qualsiasi coppia di numeri generati casualmente")

      Then("L'ordine degli addendi non deve cambiare il risultato: sum(a, b) == sum(b, a)")
      forAll { (a: Int, b: Int) =>
        sumNumbers(a, b) shouldBe sumNumbers(b, a)
      }
    }

    Scenario("Proprietà dell'incremento unitario (Property-Based)") {
      Given("Un intero generato casualmente")

      When("Gli viene sommato 1")

      Then("Il risultato deve essere esattamente n + 1 per ogni intero valido")
      forAll { (n: Int) =>
        whenever(n < Int.MaxValue) {
          sumNumbers(n, 1) shouldBe n + 1
        }
      }
    }
  }

  Feature("Gestione dell'Overflow") {

    Scenario("Somma che supera Int.MaxValue") {
      Given("Il valore massimo rappresentabile per un Int")
      val max = Int.MaxValue

      When("Si somma 1")
      val result = sumNumbers(max, 1)

      Then("Si verifica il wrap-around aritmetico a Int.MinValue")
      result shouldBe Int.MinValue
    }
  }
}


