package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DrawDecksTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  Feature("Drawing from main deck") {
    Scenario("Drawing from the main deck should return the deck without the drawn card") {
      Given("A deck with 1 card")
      val wolf = CardLibrary.wolf
      val deck = Deck.empty addCard wolf
      val drawDeck = DrawDecks(deck)

      When("drawing from the main deck")
      val (card, updatedDeck) = drawDeck.drawFromMain().get

      Then("it should return the card")
      card shouldBe wolf

      And("it should return and empty deck")
      updatedDeck.mainDeck.isEmpty shouldBe true
    }

    Scenario("Failing to draw from an empty main deck") {
      Given("An empty deck")
      val deck = Deck.empty
      val drawDeck = DrawDecks(deck)

      When("drawing from the main deck")
      val result = drawDeck.drawFromMain()

      Then("it should return None")
      result.isEmpty shouldBe true
    }

    Scenario("Drawing from the infinite squirrel deck") {
      Given("Any deck instance")
      val drawDeck = DrawDecks(Deck.empty)

      When("drawing two cards from the squirrel deck")
      val squirrel1 = drawDeck.drawFromSquirrels
      val squirrel2 = drawDeck.drawFromSquirrels

      Then("both drawn cards should be squirrels")
      squirrel1.name shouldBe "Squirrel"
      squirrel2.name shouldBe "Squirrel"

      And("they should be distinct instances")
      squirrel1.id shouldNot be (squirrel2)
    }

  }

}