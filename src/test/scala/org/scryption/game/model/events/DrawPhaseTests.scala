package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.{GUIChannel, GUIChannelInterface, FightMessages}
import org.scryption.game.model.*
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.PlayerHand.PlayerHand

class DrawPhaseTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private val wolf: CreatureCard = CreatureCard.empty withAttack 2 named "Wolf" withHealth 3

  private def createInitialFightState(): FightState =
    FightState(
      scalePoints = 0,
      bones = 0,
      deck = Deck.fromList(List(wolf, wolf, wolf, wolf)),
      playerHand = PlayerHand.empty,
      board = Board.empty
    )

  Feature("Draw Phase Handling") {

    Scenario("Drawing from squirrel deck adds a squirrel to the player hand and moves to playerTurn") {
      Given("an initial FightState and a GUIChannel with a DrawFromSquirrel message")
      val channel: GUIChannelInterface = GUIChannel.apply
      val initialFightState = createInitialFightState()

      channel.sendToGame(FightMessages.DrawFromSquirrel)

      When("handleDrawPhase is called directly")
      val (nextTurn, updatedFightState) = handleDrawPhase(initialFightState, channel)

      Then("the next state should be playerTurn")
      nextTurn shouldBe TurnState.playerTurn

      And("the player hand in the new FightState should contain the squirrel")
      updatedFightState.playerHand.toList should contain(CardLibrary.squirrel)
    }

    Scenario("Drawing from main deck adds a card to the player hand, decreases deck size, and moves to playerTurn") {
      Given("an initial FightState with a non-empty deck and a GUIChannel with a DrawFromDeck message")
      val channel: GUIChannelInterface = GUIChannel.apply
      val initialFightState = createInitialFightState()
      val initialHandSize = initialFightState.playerHand.toList.size
      val initialDeckSize = initialFightState.deck.toList.size

      channel.sendToGame(FightMessages.DrawFromDeck)

      When("handleDrawPhase is called directly")
      val (nextTurn, updatedFightState) = handleDrawPhase(initialFightState, channel)

      Then("the next state should be playerTurn")
      nextTurn shouldBe TurnState.playerTurn

      And("the player hand should have one more card than before")
      updatedFightState.playerHand.toList.size shouldBe initialHandSize + 1

      And("the deck should have one card less than before")
      updatedFightState.deck.toList.size shouldBe initialDeckSize - 1
    }


    Scenario("Attempting to draw from an empty main deck does not change FightState and remains in draw state") {
      Given("a FightState with an empty deck and a GUIChannel with a DrawFromDeck message")
      val channel: GUIChannelInterface = GUIChannel.apply

      // Creiamo uno stato con mazzo vuoto
      val emptyDeckState = createInitialFightState().copy(deck = Deck.empty)

      channel.sendToGame(FightMessages.DrawFromDeck)

      When("handleDrawPhase is called directly")
      val (nextTurn, updatedFightState) = handleDrawPhase(emptyDeckState, channel)

      Then("the turn state should remain TurnState.draw")
      nextTurn shouldBe TurnState.draw

      And("the FightState should remain identical to the initial state")
      updatedFightState shouldBe emptyDeckState
      updatedFightState.playerHand.toList shouldBe empty
      updatedFightState.deck.toList shouldBe empty
    }
  }



