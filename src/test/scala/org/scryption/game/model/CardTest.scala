package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks



class CardTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  Feature("Card immutability and basic operations") {

    Scenario("Modifying a creature's health should return a new instance") {
      Given("A basic CreatureCard with 3 health")
      val wolf = CreatureCard.empty named "Wolf" withAttack 2 withHealth 3

      When("updated its health to 4")
      val buffedWolf = wolf withHealth 4

      Then("the new card should have 4 health")
      buffedWolf.health shouldBe 4

      And("the original card should remain unchanged (3 health)")
      wolf.health shouldBe 3
    }

    Scenario("Adding a seal to a creature should return a new instance") {
      Given("A basic CreatureCard with no seals")
      val wolf = CreatureCard.empty named "Wolf"

      When("added a seal")
      val buffedWolf = wolf addSeal Seal.Immortal

      Then("the new card should have the new seal")
      buffedWolf.seals.head shouldBe Seal.Immortal

      And("the original card should remain unchanged")
      wolf.seals.isEmpty shouldBe true
    }
  }

  Feature("A card can't have negative values for its attack and health") {

    Scenario("Creating a card with attack") {
      Given("a Card")
      val card = CreatureCard.empty named "Test Card" withAttack 1

      When("setting its attack to a negative number")
      val otherCard = card withAttack -1

      Then("it should not be updated")
      otherCard shouldBe card
      otherCard.attack shouldBe 1
    }

    Scenario("Creating a card with health") {
      Given("a Card")
      val card = CreatureCard.empty named "Test Card" withHealth 1

      When("setting its health to a negative number")
      val otherCard = card withAttack -1

      Then("it should not be updated")
      otherCard shouldBe card
      otherCard.health shouldBe 1
    }
  }


}
