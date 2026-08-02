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

    Scenario("Adding a card to the hand") {
      Given("An empty hand and a card")
      val hand = PlayerHand.empty
      val squirrel = CardLibrary.squirrel

      When("the card is added to the hand")
      val updatedHand = hand addCard squirrel

      Then("the hand should contain 1 card")
      updatedHand.size shouldBe 1
      updatedHand.toList should contain(squirrel)
    }

  }

}
