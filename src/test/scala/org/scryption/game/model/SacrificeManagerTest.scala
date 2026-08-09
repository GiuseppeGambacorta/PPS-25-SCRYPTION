package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.{BoardRow, x, |, Board}

class SacrificeManagerTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val squirrel = CardLibrary.squirrel

  Feature("Sacrifice resolution on the board"):
    Scenario("Sacrificing a normal card removes it from the board"):
      Given("A board with a normal squirrel at row 0, column 0")
      val row0: BoardRow = Some(squirrel) | x | x | x
      val board: Board = Board(row0, BoardRow.empty, BoardRow.empty)
      val sacrificedSlots = List((0, 0))

      When("resolving the sacrifice")
      val updatedBoard = SacrificeManager.resolveSacrifices(board, sacrificedSlots)

      Then("the squirrel should be removed and the slot (0, 0) should be empty")
      updatedBoard(0)(0) shouldBe x
