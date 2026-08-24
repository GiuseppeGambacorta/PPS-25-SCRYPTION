package org.scryption.game.model.bot

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.{Board, BoardRow, IndexOfBotPrepRow, IndexOfBotRow, IndexOfPlayerRow, |}
import org.scryption.game.model.events.FightState
import org.scryption.game.model.{CardLibrary, Deck, PlayerHand}

class RandomBotStrategyTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private def createDummyState(board: Board): FightState =
    FightState(0, 0, Deck.empty, PlayerHand.empty, board, List.empty)

  Feature("Random Bot Strategy"):
    val botStrategy = RandomBotStrategy()

    Scenario("The bot plays cards only in the preparation row (row 0)"):
      Given("A completely empty board")
      val emptyBoard = Board.empty
      val initialState = createDummyState(emptyBoard)

      When("the random bot plays its turn")
      val newState = botStrategy.playTurn(initialState)

      Then("the bot's attack row and player's row must remain exactly as they were")
      newState.board(IndexOfBotRow) shouldBe emptyBoard(IndexOfBotRow)
      newState.board(IndexOfPlayerRow) shouldBe emptyBoard(IndexOfPlayerRow)

    Scenario("The bot must not overwrite existing cards in the preparation row"):

      Given("A board with the preparation row fully occupied by Wolves")
      val fullPrepRow: BoardRow =
        Some(CardLibrary.wolf) | Some(CardLibrary.wolf) | Some(CardLibrary.wolf) | Some(CardLibrary.wolf)
      val fullBoard = Board.empty.updateRow(IndexOfBotPrepRow, fullPrepRow)
      val initialState = createDummyState(fullBoard)

      When("the random bot plays its turn")
      val newState = botStrategy.playTurn(initialState)

      Then("the preparation row must remain exactly the same")
      newState.board(IndexOfBotPrepRow) shouldBe fullPrepRow
