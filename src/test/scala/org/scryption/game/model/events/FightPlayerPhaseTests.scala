package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.{GUIChannel, GUIChannelInterface, GameMessage}
import org.scryption.game.model.*
import org.scryption.game.model.boardModel.*

class FightPlayerPhaseTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  // Carte Giocatore (Riga in basso - Index 2)
  private val squirrel: CreatureCard = CreatureCard.empty withAttack 1 named "squirrel" withHealth 1
  private val wolf: CreatureCard     = CreatureCard.empty withAttack 3 named "Wolf" withHealth 2
  private val bear: CreatureCard     = CreatureCard.empty withAttack 4 named "Bear" withHealth 6

  // Carte Bot / Opponente (Riga centrale - Index 1)
  private val adder: CreatureCard    = CreatureCard.empty withAttack 1 named "Adder" withHealth 2
  private val bullfrog: CreatureCard = CreatureCard.empty withAttack 1 named "Bullfrog" withHealth 3

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
      board = board,
      inventory = List.empty
    )

  Feature("Player Fight Phase - Direct Attacks and Creature Combat") {

    Scenario("Player creature with NO opponent in the central row attacks the opponent directly") {
      Given("a player Wolf (3 ATK) in player row (index 2) and empty central row (index 1)")
      val channel: GUIChannelInterface = GUIChannel.getNewChannel

      val initialBoard = (x          | x | x | x) || // Index 0: Bot row
        (x          | x | x | x) || // Index 1: Central row (Empty)
        (Some(wolf) | x | x | x)    // Index 2: Player row

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0)

      When("handleFightPhase is called for player attacking")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = true)

      Then("scale points should increase by the full attack damage of the Wolf (3 points)")
      updatedState.scalePoints shouldBe 3

      And("the next turn should advance to botTurn")
      nextTurn shouldBe TurnState.botTurn
    }

    Scenario("Player creature fights directly with the opponent creature placed in the central row") {
      Given("a player Wolf (3 ATK) in index 2 and a Bullfrog (3 HP) in the central row (index 1)")
      val channel: GUIChannelInterface = GUIChannel.getNewChannel

      val initialBoard = (x              | x | x | x) || // Index 0: Bot row
        (Some(bullfrog) | x | x | x) || // Index 1: Central row (Opponent card)
        (Some(wolf)     | x | x | x)    // Index 2: Player row

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0)

      When("handleFightPhase is called")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = true)

      Then("scale points should NOT increase because the attack was blocked by the creature in the central row")
      updatedState.scalePoints shouldBe 0

      And("the Bullfrog in the central row should receive 3 damage and be destroyed")
      updatedState.board(1)(0) shouldBe None

      And("bones should increase by 1 for destroying an opponent creature")
      updatedState.bones shouldBe 1
    }

    Scenario("Player creature attacks opponent in central row, but does not destroy it if HP > ATK") {
      Given("a player Squirrel (1 ATK) in index 2 facing an Adder (2 HP) in the central row (index 1)")
      val channel: GUIChannelInterface = GUIChannel.getNewChannel

      val initialBoard = (x              | x | x | x) || // Index 0: Bot row
        (Some(adder)    | x | x | x) || // Index 1: Central row
        (Some(squirrel) | x | x | x)    // Index 2: Player row

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 0)

      When("handleFightPhase is called")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = true)

      Then("scale points should remain 0")
      updatedState.scalePoints shouldBe 0

      And("the Adder in the central row should remain on board with 1 HP")
      val damagedAdder = updatedState.board(1)(0)
      damagedAdder shouldBe defined
      damagedAdder.get.health shouldBe 1
    }

    Scenario("Mixed Board: Column 0 fights central row creature, Column 1 attacks directly") {
      Given("Wolf (col 0) facing Bullfrog in central row, and Bear (col 1) facing empty central row slot")
      val channel: GUIChannelInterface = GUIChannel.getNewChannel

      // Col 0: Wolf (3 ATK) vs Bullfrog (3 HP) in central row -> Bullfrog dies, 0 scale damage
      // Col 1: Bear (4 ATK) vs Empty in central row           -> 4 direct scale damage
      val initialBoard = (x              | x          | x | x) || // Index 0: Bot row
        (Some(bullfrog) | x          | x | x) || // Index 1: Central row
        (Some(wolf)     | Some(bear) | x | x)    // Index 2: Player row

      val initialState = createInitialFightState(board = initialBoard, scalePoints = 2, bones = 0)

      When("handleFightPhase is called")
      val (nextTurn, updatedState) = handleFightPhase(initialState, channel, isPlayerAttacking = true)

      Then("scale points should increase by 4 (from 2 to 6, reaching win condition)")
      updatedState.scalePoints shouldBe 6

      And("the creature in the central row at col 0 should be destroyed and grant 1 bone")
      updatedState.board(1)(0) shouldBe None
      updatedState.bones shouldBe 1

      And("next turn state should be botTurn")
      nextTurn shouldBe TurnState.botTurn
    }
  }