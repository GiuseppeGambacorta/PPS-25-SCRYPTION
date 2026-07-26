package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.Deck.*

class DeckTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  Feature("Deck basic operations") {

    Scenario("Creating an empty deck") {
      Given("An empty deck factory")
      val deck = Deck.empty

      Then("its size should be 0")
      deck.size shouldBe 0

      And("it should be considered empty")
      deck.isEmpty shouldBe true
    }

    Scenario("Adding a card to an empty deck") {
      Given("An empty deck and a mock card")
      val emptyDeck = Deck.empty
      val squirrel = CreatureCard.empty named "Squirrel"

      When("the card is added to the deck")
      val deckWithOneCard = emptyDeck addCard squirrel

      Then("the new deck should have size 1")
      deckWithOneCard.size shouldBe 1

      And("it should not be empty anymore")
      deckWithOneCard.isEmpty shouldBe false

      And("the original deck should remain empty")
      emptyDeck.size shouldBe 0
      emptyDeck.isEmpty shouldBe true
    }

    Scenario("Drawing from an empty deck should safely return None") {
      Given("An empty deck")
      val emptyDeck = Deck.empty

      When("trying to draw a card")
      val result = emptyDeck.draw

      Then("it should return None")
      result shouldBe None
    }

    Scenario("Drawing a card from a non-empty deck") {
      Given("A deck with two cards")
      val squirrel = CreatureCard.empty named "Squirrel"
      val wolf = CreatureCard.empty named "Wolf"
      val deck = Deck.empty addCard squirrel addCard wolf

      When("a card is drawn")
      val (drawnCard, newDeck) = deck.draw.get

      Then("the drawn card should be the last one we added")
      drawnCard.name shouldBe "Wolf"

      And("the new deck should contain the first added card")
      newDeck.size shouldBe 1
      val (secondDrawnCard, emptyDeck) = newDeck.draw.get
      secondDrawnCard.name shouldBe "Squirrel"
      emptyDeck.size shouldBe 0
    }

  }
  /*
  Feature("Changing the deck") {

    Scenario("Removing a card from the Deck") {
      Given("a Deck")
      val deck =
        Deck.getDeckFromList(List(CardLibrary.squirrel, CardLibrary.bear, CardLibrary.squirrel, CardLibrary.bear))

      When("remove a card")
      val card = CardLibrary.squirrel
      val newDeck = deck removeCard card

      Then("deck should not have the card removed")
      newDeck shouldBe Deck.getDeckFromList(List(CardLibrary.bear, CardLibrary.squirrel, CardLibrary.bear))

    }

  }
*/
}
