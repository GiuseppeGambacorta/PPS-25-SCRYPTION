package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.Deck.*

class deckTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

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

}
