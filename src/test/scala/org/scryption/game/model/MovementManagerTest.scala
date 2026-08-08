package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.{BoardRow, x, |}

class MovementManagerTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val elkRight = CreatureCard.empty withAttack 2 named "Elk" withHealth 4 addSeal Seal.Sprinter(Direction.Right)
  private val elkLeft = elkRight removeSeal Seal.Sprinter(Direction.Right) addSeal Seal.Sprinter(Direction.Left)
  private val boulder = CardLibrary.boulder

  Feature("Sprinter seal movement resolution"):
    Scenario("Sprinter(Right) moves to the right if the slot is empty"):
      Given("A row with an elk moving Right at column 1")
      val row: BoardRow = x | Some(elkRight) | x | x

      When("resolving end of turn movements")
      val updatedRow = MovementManager.resolveRowMovements(row)

      Then("the elk should physically move to column 2")
      updatedRow(1) shouldBe x
      updatedRow(2) shouldBe Some(elkRight)

    Scenario("Sprinter(Right) hits an obstacle, changes direction to left and moves left"):
      Given("A row with an elk moving right at column 1 and a boulder at column 2")
      val row: BoardRow = x | Some(elkRight) | Some(boulder) | x

      When("resolving end of turn movements")
      val updatedRow = MovementManager.resolveRowMovements(row)

      Then("the elk, blocked by the boulder, should change to Sprinter(Left) and move to col 0")
      updatedRow(1) shouldBe x
      updatedRow(0) shouldBe Some(elkLeft)

      And("the boulder should remain unchanged in col 2")
      updatedRow(2) shouldBe Some(boulder)

    Scenario("Sprinter(Left) moves to the left if the slot is empty"):
      Given("A row with an elk moving left at column 2")
      val row: BoardRow = x | x | Some(elkLeft) | x

      When("resolving end of turn movements")
      val updatedRow = MovementManager.resolveRowMovements(row)

      Then("the elk should physically move to column 1")
      updatedRow(2) shouldBe x
      updatedRow(1) shouldBe Some(elkLeft)

    Scenario("Sprinter(Left) at the left edge bounces and changes direction to right"):
      Given("A row with an elk moving left at the leftmost edge (column 0)")
      val row: BoardRow = Some(elkLeft) | x | x | x

      When("resolving end of turn movements")
      val updatedRow = MovementManager.resolveRowMovements(row)

      Then("the elk should change to Sprinter(Right) and move to col 1")
      updatedRow(0) shouldBe x
      updatedRow(1) shouldBe Some(elkRight)