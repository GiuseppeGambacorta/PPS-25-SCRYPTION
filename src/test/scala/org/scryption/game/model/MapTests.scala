package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.boardModel.*

class MapTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

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

    
  
  }