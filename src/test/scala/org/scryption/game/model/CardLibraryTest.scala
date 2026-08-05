package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

/*
class CardLibraryTest extends AnyFeatureSpec with GivenWhenThen with Matchers {

  Feature("Card generation from Library") {

    Scenario("Generating random Common cards") {
      Given("A request to generate 3 Common cards")
      val amount = 3

      When("the cards are generated from the library")
      val cards = CardLibrary.generateRandomCards(Rarity.Common, amount)

      Then("exactly 3 cards should be returned")
      cards.size shouldBe amount

      And("they should all be of Common rarity")
      cards.foreach { card =>
        card.rarity shouldBe Rarity.Common
      }
    }

    Scenario("Generating random Rare cards") {
      Given("A request to generate 2 Rare cards")
      val amount = 2

      When("the cards are generated from the library")
      val cards = CardLibrary.generateRandomCards(Rarity.Rare, amount)

      Then("exactly 2 cards should be returned")
      cards.size shouldBe amount

      And("they should all be of Rare rarity")
      cards.foreach(card => card.rarity shouldBe Rarity.Rare)
    }

    Scenario("Generating cards deterministically using a seed") {
      Given("A fixed seed and a request for 4 Common cards")
      val fixedSeed = 12345L
      val amount = 4

      When("generating the lists twice with the exact same seed")
      val firstGeneration = CardLibrary.generateRandomCards(Rarity.Common, amount, fixedSeed)
      val secondGeneration = CardLibrary.generateRandomCards(Rarity.Common, amount, fixedSeed)

      Then("the two generated lists should have the exact same sequence of cards")
      val firstNames = firstGeneration.map(_.name)
      val secondNames = secondGeneration.map(_.name)

      firstNames shouldBe secondNames
    }

    Scenario("Generating fresh instances for every call") {
      Given("A generation of 2 Common cards")
      val cards = CardLibrary.generateRandomCards(Rarity.Common, 2)

      When("checking the generated cards")
      val card1 = cards.head
      val card2 = cards.last

      Then("they must have strictly distinct UUIDs, even if they have the same name")
      card1.id shouldNot be (card2.id)
    }
  }
}
*/