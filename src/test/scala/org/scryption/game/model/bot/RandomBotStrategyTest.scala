package org.scryption.game.model.bot

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.{Board, ColsCount, IndexOfBotPrepRow, IndexOfBotRow, IndexOfPlayerRow}
import org.scryption.game.model.events.FightState
import org.scryption.game.model.{Deck, PlayerHand}

class RandomBotStrategyTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private def createDummyState(board: Board): FightState =
    FightState(0, 0, Deck.empty, PlayerHand.empty, board)

  Feature("Random Bot Strategy"):
    val botStrategy = RandomBotStrategy

    Scenario("The bot plays cards ONLY in the preparation row (row 0)"):
      Given("A completely empty board")
      val emptyBoard = Board.empty
      val initialState = createDummyState(emptyBoard)

      When("the random bot plays its turn")
      val newState = botStrategy.playTurn(initialState)

      Then("the bot's attack row and player's row MUST remain exactly as they were")
      newState.board(IndexOfBotRow) shouldBe emptyBoard(IndexOfBotRow)
      newState.board(IndexOfPlayerRow) shouldBe emptyBoard(IndexOfPlayerRow)