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

    Scenario("Removing a card from the hand by ID") {
      Given("A hand with a card")
      val grizzly = CardLibrary.grizzly
      val hand = PlayerHand.empty addCard grizzly

      When("the card is removed")
      val emptyHand = hand removeCard grizzly

      Then("the hand should be empty")
      emptyHand.isEmpty shouldBe true
    }

    Scenario("Adding multiple cards to the hand") {
      Given("A hand with one card and a list of new cards")
      val squirrel = CardLibrary.squirrel
      val grizzly = CardLibrary.grizzly
      val hand = PlayerHand.empty addCard squirrel
      val newCards = List(grizzly, grizzly)

      When("the multiple cards are added to the hand")
      val updatedHand = hand addCards newCards

      Then("the hand size should increase accordingly")
      updatedHand.size shouldBe 3

      And("it should contain all the cards")
      updatedHand.toList should contain theSameElementsAs (grizzly :: grizzly :: squirrel :: Nil)
    }

    Scenario("Extracting a specific card from the hand") {
      Given("A hand with two different cards")
      val squirrel = CardLibrary.squirrel
      val grizzly = CardLibrary.grizzly
      val hand = PlayerHand.empty addCard squirrel addCard grizzly

      When("extracting the grizzly card using its ID")
      val extractionResult = hand.extractCard(grizzly)

      Then("it should return the grizzly card and a new hand without it")
      extractionResult.isDefined shouldBe true
      val (extractedCard, updatedHand) = extractionResult.get
      extractedCard.id shouldBe grizzly.id
      updatedHand.size shouldBe 1
      updatedHand.toList should contain(squirrel)
      updatedHand.toList shouldNot contain(grizzly)
    }

    Scenario("Failing to extract a card that is not in the hand") {
      Given("A hand with a squirrel")
      val squirrel = CardLibrary.squirrel
      val grizzly = CardLibrary.grizzly
      val hand = PlayerHand.empty addCard squirrel

      When("attempting to extract the grizzly")
      val extractionResult = hand.extractCard(grizzly)

      Then("it should safely return None")
      extractionResult.isEmpty shouldBe true
    }

  }

}
