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

    Scenario("Setting a Blood sacrifice attribute should return a new instance") {
      Given("A basic CreatureCard with no cost")
      val wolf = CreatureCard.empty named "Wolf"

      When("updated its sacrifice attribute to 2 Blood")
      val costWolf = wolf withSacrificeAttribute SacrificeAttribute.Blood(2)

      Then("the new card should cost 2 Blood")
      costWolf.sacrificeAttribute shouldBe SacrificeAttribute.Blood(2)

      And("the original card should remain unchanged")
      wolf.sacrificeAttribute shouldBe SacrificeAttribute.Nil()
    }

    Scenario("Setting a Bones sacrifice attribute should return a new instance") {
      Given("A basic CreatureCard with no cost")
      val skeleton = CreatureCard.empty named "Skeleton"

      When("updated its sacrifice attribute to 4 bones")
      val boneSkeleton = skeleton withSacrificeAttribute SacrificeAttribute.Bones(4)

      Then("the new card should cost 4 Bones")
      boneSkeleton.sacrificeAttribute shouldBe SacrificeAttribute.Bones(4)
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

  Feature("Support Cards logic") {

    Scenario("Creating and modifying a SupportCard") {
      Given("A SupportCard")
      val boulder = SupportCard.empty named "Boulder" withHealth 5 withSacrificeAttribute SacrificeAttribute.Nil() addSeal Seal.Wall

      Then("it should correctly update all valid attributes")
      boulder.name shouldBe "Boulder"
      boulder.health shouldBe 5
      boulder.seals.head shouldBe Seal.Wall
    }
  }

}
