package org.scryption.game.model.items

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.{Deck, PlayerHand}
import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.events.FightState

class GameItemTestTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private def createInitialState(bottle: GameItem) =
    FightState(
      scalePoints = 0,
      bones = 0,
      deck = Deck.empty,
      playerHand = PlayerHand.empty,
      board = Board.empty,
      inventory = List(bottle)
    )

  Feature("Game Items"):

    Scenario("Using the Squirrel Bottle adds a Squirrel to the hand and removes the item from inventory"):
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

    Scenario("Hoggy Bank adds 4 bones and is removed from inventory"):
      Given("A FightState with 0 bones and the Hoggy Bank in inventory")
      val hoggyBank = HoggyBank()
      val initialState = createInitialState(hoggyBank)

      When("the player uses the Hoggy Bank")
      val newState = hoggyBank.use(initialState)

      Then("the player has exactly 4 bones")
      newState.bones shouldBe 4

      And("the Hoggy Bank is removed from the inventory")
      newState.inventory shouldBe empty

    Scenario("Pliers adds 1 point to the scale and is removed from inventory"):
      Given("A FightState with 0 scale points and Pliers in inventory")
      val pliers = Pliers()
      val initialState = createInitialState(pliers)

      When("the player uses the Pliers")
      val newState = pliers.use(initialState)

      Then("the scale points MUST increase by 1")
      newState.scalePoints shouldBe 1
      And("the Pliers MUST be removed from the inventory")
      newState.inventory shouldBe empty
