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
      // Ipotizziamo che ci sia un companion object con un metodo 'empty'
      val deck = Deck.empty

      Then("its size should be 0")
      deck.size shouldBe 0

      And("it should be considered empty")
      deck.isEmpty shouldBe true
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
