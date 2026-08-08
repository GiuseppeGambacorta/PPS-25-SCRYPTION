package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.{BoardRow, x, |}

class MovementManagerTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val elkRight = CreatureCard.empty withAttack 2 named "Elk" withHealth 4 addSeal Seal.Sprinter(Direction.Right)

  Feature("Sprinter seal movement resolution"):
    Scenario("Sprinter(Right) moves to the right if the slot is empty"):
      Given("A row with an elk moving Right at column 1")
      val row: BoardRow = x | Some(elkRight) | x | x

      When("resolving end of turn movements")
      val updatedRow = MovementManager.resolveRowMovements(row)

      Then("the elk should physically move to column 2")
      updatedRow(1) shouldBe x
      updatedRow(2) shouldBe Some(elkRight)

