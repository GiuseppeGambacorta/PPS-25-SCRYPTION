package org.scryption.game.model.items

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.{Deck, PlayerHand}
import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.events.FightState

class GameItemTestTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private def createInitialState(bottle: SquirrelBottle) =
    FightState(
      scalePoints = 0,
      bones = 0,
      deck = Deck.empty,
      playerHand = PlayerHand.empty,
      board = Board.empty,
      inventory = List(bottle)
    )

  Feature("Squirrel in a Bottle"):

    Scenario("Using the bottle adds a Squirrel to the hand and removes the item from inventory"):
      Given("A FightState with 0 cards in hand and the bottle in inventory")
      val bottle = SquirrelBottle()
      val initialState: FightState = createInitialState(bottle)

      When("the player uses the Squirrel in a Bottle")
      val newState = bottle.use(initialState)

      Then("the player's hand contain exactly one card, which is a Squirrel")
      newState.playerHand.toList should have size 1
      newState.playerHand.toList.head.name shouldBe "Squirrel"

      And("the bottle is removed from the inventory")
      newState.inventory shouldBe empty
