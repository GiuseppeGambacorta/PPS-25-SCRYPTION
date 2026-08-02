package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class PlayerHandTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  Feature("Player Hand Management") {

    Scenario("Creating an empty hand") {
      Given("An empty player hand")
      val hand = PlayerHand.empty

      Then("it should have size 0")
      hand.size shouldBe 0
      hand.isEmpty shouldBe true
    }

  }

}
