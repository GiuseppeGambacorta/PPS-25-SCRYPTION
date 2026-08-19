package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.{GUIChannel, GUIChannelInterface, GameMessage}
import org.scryption.game.model.*
import org.scryption.game.model.boardModel.*

class FightBotPhaseTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  // Carte Bot / Opponente (Attaccano dalla riga centrale - Index 1)
  private val adder: CreatureCard    = CreatureCard.empty withAttack 1 named "Adder" withHealth 2
  private val raven: CreatureCard    = CreatureCard.empty withAttack 2 named "Raven" withHealth 3
  private val wolf: CreatureCard     = CreatureCard.empty withAttack 3 named "Wolf" withHealth 2

  // Carte Giocatore (Difendono nella riga in basso - Index 2)
  private val squirrel: CreatureCard = CreatureCard.empty withAttack 1 named "squirrel" withHealth 1
  private val bear: CreatureCard     = CreatureCard.empty withAttack 4 named "Bear" withHealth 6

  private def createInitialFightState(
                                       board: Board = Board.empty,
                                       scalePoints: Int = 0,
                                       bones: Int = 0
                                     ): FightState =
    FightState(
      scalePoints = scalePoints,
      bones = bones,
      deck = Deck.empty,
      playerHand = PlayerHand.empty,
      board = board
    )

  Feature("Bot Fight Phase - Attacks From Central Row Against Player Row") {

    Scenario("Bot creature in central row attacks player directly when player slot is empty") {
      Given("a Bot Raven (2 ATK) in the central row (index 1) and empty player slot (index 2)")
      val channel: GUIChannelInterface = GUIChannel.apply

      val initialBoard = (x           | x | x | x) || // Index 0: Bot back row
        (Some(raven) | x | x | x) || // Index 1: Central row (Bot attacking card)
        (x           | x | x | x)    // Index 2: Player row (Empty)

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0)

      When("handleFightPhase is called with isPlayerAttacking = false")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = false)

      Then("scale points should DECREASE by Raven's attack damage (-2 points)")
      updatedState.scalePoints shouldBe -2

      And("the next turn should return to draw phase")
      nextTurn shouldBe TurnState.draw
    }

    Scenario("Bot creature in central row fights player creature in player row") {
      Given("a Bot Wolf (3 ATK) in index 1 facing a Player Bear (6 HP) in index 2")
      val channel: GUIChannelInterface = GUIChannel.apply

      val initialBoard = (x          | x | x | x) || // Index 0: Bot back row
        (Some(wolf) | x | x | x) || // Index 1: Central row (Bot card)
        (Some(bear) | x | x | x)    // Index 2: Player row (Player card)

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0)

      When("handleFightPhase is called with isPlayerAttacking = false")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = false)

      Then("scale points should remain unchanged (0 delta)")
      updatedState.scalePoints shouldBe 0

      And("the Player Bear should take 3 damage and remain on board with 3 HP")
      val damagedBear = updatedState.board(2)(0)
      damagedBear shouldBe defined
      damagedBear.get.health shouldBe 3
    }

    Scenario("Bot destroys Player creature and player gains bones for lost creature") {
      Given("a Bot Wolf (3 ATK) in index 1 facing a Player Squirrel (1 HP) in index 2")
      val channel: GUIChannelInterface = GUIChannel.apply

      val initialBoard = (x              | x | x | x) || // Index 0: Bot back row
        (Some(wolf)     | x | x | x) || // Index 1: Central row (Bot card)
        (Some(squirrel) | x | x | x)    // Index 2: Player row (Player card)

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0, bones = 0)

      When("handleFightPhase is called with isPlayerAttacking = false")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = false)

      Then("the Player Squirrel should be destroyed")
      updatedState.board(2)(0) shouldBe None

      And("the player should receive 1 bone for the death of their creature")
      updatedState.bones shouldBe 1

      And("scale points remain 0")
      updatedState.scalePoints shouldBe 0
    }

    Scenario("Bot direct attacks push scale points to BotWinningPoints (-6)") {
      Given("Bot creatures with total 4 ATK facing empty player slots and scale points already at -3")
      val channel: GUIChannelInterface = GUIChannel.apply

      val initialBoard = (x           | x          | x | x) ||
        (Some(raven) | Some(raven)| x | x) || // 2 ATK + 2 ATK = 4 ATK
        (x           | x          | x | x)

      val initialState = createInitialFightState(board = initialBoard, scalePoints = -3)

      When("handleFightPhase is called with isPlayerAttacking = false")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = false)

      Then("scale points should decrease to -7 (reaching or passing BotWinningPoints threshold of -6)")
      updatedState.scalePoints shouldBe -7
      updatedState.scalePoints should be <= -6

      And("the next state returns to draw where game over will be evaluated")
      nextTurn shouldBe TurnState.draw
    }
  }