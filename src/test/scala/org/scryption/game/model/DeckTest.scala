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

    Scenario("Shuffling a deck deterministically with a seed") {
      Given("A deck with multiple distinct cards and a fixed seed")
      val squirrel = CreatureCard.empty named "Squirrel"
      val wolf = CreatureCard.empty named "Wolf"
      val stoat = CreatureCard.empty named "Stoat"
      val originalDeck = Deck.empty addCard squirrel addCard wolf addCard stoat
      val fixedSeed = 42

      When("the deck is shuffled twice using the same seed")
      val shuffledDeck1 = originalDeck.shuffle(fixedSeed)
      val shuffledDeck2 = originalDeck.shuffle(fixedSeed)

      Then("the size should remain the same")
      shuffledDeck1.size shouldBe originalDeck.size

      And("both shuffled decks should have the exact same card order")
      shuffledDeck1.toList shouldBe shuffledDeck2.toList

      And("they should contain the exact same elements as the original")
      shuffledDeck1.toList should contain theSameElementsAs originalDeck.toList
    }

    Scenario("Removing a single instance of a card from the Deck") {
      Given("a Deck with duplicate cards")
      val squirrel = CreatureCard.empty named "Squirrel"
      val bear = CreatureCard.empty named "Bear"
      val deck = Deck.fromList(List(squirrel, bear, squirrel, bear))

      When("a specific card is removed")
      val newDeck = deck removeCard squirrel

      Then("the deck should have exactly one instance of that card removed")
      newDeck.toList shouldBe List(bear, squirrel, bear)
    }

  }
}
