package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks



class FeatureSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {



  Feature("A card can't have negative life") {

    Scenario("Creating a card") {
      Given("a Card")
      val card = Card.card Named "Test Card"

      When("Vengono sommati insieme")
      val othercard = card WithAttack(-1)

      Then("Il risultato deve essere 15")
      othercard shouldBe null
    }


  }


}
