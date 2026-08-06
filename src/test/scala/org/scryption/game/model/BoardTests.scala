package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.boardModel.*

class BoardTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private val squirrel: CreatureCard = CreatureCard.empty withAttack 1 named "squirrel" withHealth 1
  private val bear: CreatureCard = CreatureCard.empty withAttack 2 named "bear" withHealth 3
  private val fox: CreatureCard = CreatureCard.empty withAttack 1 named "fox" withHealth 1
  private val wolf: CreatureCard = CreatureCard.empty withAttack 1 named "Wolf" withHealth 1

  Feature("Creation of a Row of the Board") {

    Scenario("The Row must be empty") {
      Given("an empty BoardRow")
      val row = BoardRow.empty

      Then("The resulting row should not have cards inside")
      row.numberOfCards shouldBe 0
    }

    Scenario("Creating a row from a set of cards") {
      Given("a set of cards")
      val cards = List(squirrel, bear, fox)

      When("creating a new row with these cards")
      val row = BoardRow(Some(squirrel), Some(bear), Some(fox), x)

      Then("the row should contain the same number of cards")
      row.numberOfCards shouldBe cards.length

      And("the cards should be in the correct positions")
      row(0) shouldBe Some(squirrel)
      row(1) shouldBe Some(bear)
      row(2) shouldBe Some(fox)
      row(3) shouldBe None
    }

    Scenario("Not creating a row from a set of cards, because i am not inserting 4 cards") {
      When("attempting to create a row with fewer than 4 slots")
      Then("an IllegalArgumentException should be thrown")
      an[IllegalArgumentException] should be thrownBy {
        BoardRow(Some(squirrel), Some(bear), Some(fox))
      }
    }

    Scenario("Creating a row using the DSL") {
      Given("a set of cards")
      val cards = List(squirrel, bear, fox)

      When("creating a new row with these cards")
      val row = Some(squirrel) | Some(bear) | Some(fox) | x

      Then("the row should contain the same number of cards")
      row.numberOfCards shouldBe cards.length

      And("the cards should be in the correct positions")
      row(0) shouldBe Some(squirrel)
      row(1) shouldBe Some(bear)
      row(2) shouldBe Some(fox)
      row(3) shouldBe None
    }

    Scenario("Creating a row using the DSL, but exceeding the maximum number of slots") {
      When("attempting to create a row with more than 4 slots")
      Then("an IllegalArgumentException should be thrown")
      an[IllegalArgumentException] should be thrownBy {
        Some(squirrel) | Some(bear) | Some(fox) | x | x
      }
    }
  }

  Feature("Updating a Row") {

    Scenario("Updating a specific slot in a BoardRow") {
      Given("an empty row")
      val row = BoardRow.empty
      row.numberOfCards shouldBe 0

      When("updating the slot at col 2 with a card")
      val updatedRow = row.updated(2, Some(bear))

      Then("the row should reflect the new card without mutating the original row")
      row(2) shouldBe None
      updatedRow(2) shouldBe Some(bear)
      updatedRow.numberOfCards shouldBe 1
    }
  }

  Feature("Creation of the Board") {

    Scenario("Creating an empty board") {
      When("instantiating an empty board")
      val board = Board.empty

      Then("the board should not have any cards")
      board.numberOfCards shouldBe 0
    }

    Scenario("Creating an empty board with RowDSL") {
      When("creating a board using the || operator")
      val board = (x | x | x | x) ||
                  (x | x | x | x) ||
                  (x | x | x | x)

      Then("the board should contain 0 cards")
      board.numberOfCards shouldBe 0
    }

    Scenario("Creating a board from 3 rows using Board.apply and verifying card positions") {
      Given("three rows created with DSL containing cards")
      val row0 = Some(squirrel) | x              | x          | x
      val row1 = x              | Some(bear)     | x          | x
      val row2 = x              | x              | Some(fox)  | Some(wolf)

      When("creating a board using Board.apply")
      val board = Board(row0, row1, row2)

      Then("the board should contain the total count of cards")
      board.numberOfCards shouldBe 4

      And("the cards should be present at the exact coordinates (row, col)")
      board(0)(0) shouldBe Some(squirrel)
      board(1)(1) shouldBe Some(bear)
      board(2)(2) shouldBe Some(fox)
      board(2)(3) shouldBe Some(wolf)

      And("empty slots should return None")
      board(0)(1) shouldBe None
      board(1)(0) shouldBe None
    }

    Scenario("Attempting to create a 3-row board where one row has only 3 slots should throw an exception") {
      When("attempting to combine 3 rows using DSL where one row is missing a slot")
      Then("an IllegalArgumentException should be thrown")
      an[IllegalArgumentException] should be thrownBy {
        (x | x | x | x) ||
        (x | x | x)     ||
        (x | x | x | x)
      }
    }

    Scenario("Attempting to create a board with 4 rows directly using DSL should throw an exception") {
      When("attempting to chain 4 rows directly with the || operator")
      Then("an IllegalArgumentException should be thrown")
      an[IllegalArgumentException] should be thrownBy {
          (x | x | x | x) ||
          (x | x | x | x) ||
          (x | x | x | x) ||
          (x | x | x | x)
      }
    }
  }

  Feature("Updating the Board") {

    Scenario("Updating a slot directly on the Board using updatedSlot") {
      Given("a board created using DSL")
      val initialBoard = (Some(squirrel) | x          | x | x) ||
                         (x              | Some(bear) | x | x) ||
                         (x              | x          | x | x)

      initialBoard.numberOfCards shouldBe 2

      When("updating slot at row 2, col 3 with a wolf")
      val updatedBoard = initialBoard.updatedSlot(2, 3, Some(wolf))

      Then("the new board should reflect the added card while leaving the original board unchanged")
      initialBoard(2)(3) shouldBe None
      updatedBoard(2)(3) shouldBe Some(wolf)

      And("the total card count should increase")
      updatedBoard.numberOfCards shouldBe 3

      And("the pre-existing cards should remain intact")
      updatedBoard(0)(0) shouldBe Some(squirrel)
      updatedBoard(1)(1) shouldBe Some(bear)
    }

    Scenario("Replacing an entire row on the Board using updateRow and DSL") {
      Given("a board initialized with empty rows using DSL")
      val initialBoard = (x | x | x | x) ||
                         (x | x | x | x) ||
                         (x | x | x | x)

      initialBoard.numberOfCards shouldBe 0

      When("creating a new row with DSL and updating row index 1")
      val newRow = Some(wolf) | Some(fox) | x | Some(bear)
      val updatedBoard = initialBoard.updateRow(1, newRow)

      Then("the specified row should contain the new cards")
      updatedBoard(1)(0) shouldBe Some(wolf)
      updatedBoard(1)(1) shouldBe Some(fox)
      updatedBoard(1)(2) shouldBe None
      updatedBoard(1)(3) shouldBe Some(bear)

      And("the other rows should remain empty")
      updatedBoard(0).numberOfCards shouldBe 0
      updatedBoard(2).numberOfCards shouldBe 0

      And("the total number of cards on the board should equal the new row's card count")
      updatedBoard.numberOfCards shouldBe newRow.numberOfCards
    }
  }