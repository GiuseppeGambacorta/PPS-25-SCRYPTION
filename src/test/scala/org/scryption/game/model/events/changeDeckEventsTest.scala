package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.{Card, CreatureCard,Seal}
import org.scryption.game.model.Deck.*
import org.scryption.game.model.Seal.{Sprinter, Wall}
import org.scryption.GUIMessages
import org.scryption.GUIChannel

object TestCards:
  val squirrel: CreatureCard = CreatureCard.empty withAttack(1) named("squirrel") withHealth(1)
  val bear: CreatureCard = CreatureCard.empty withAttack(2) named("bear") withHealth(3)
  val fox: CreatureCard = CreatureCard.empty withAttack(1) named("fox") withHealth(1)

  val card1: CreatureCard = CreatureCard.empty withAttack(0) named("firstCard") withHealth(1) addSeal(Sprinter) addSeal(Wall)
  val card2: CreatureCard = CreatureCard.empty withAttack(0) named("secondCard") withHealth(1)

class ChangeDeckEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers:

  Feature("Events that change the deck") {

    Scenario("Adding a new card via GetANewCard") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(TestCards.squirrel :: TestCards.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(TestCards.squirrel))

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)

      Then("The resulting deck should equal the initial deck with the chosen card added")
      updatedGameState.deck shouldBe (initialDeck addCard TestCards.squirrel)
      updatedGameState.deck.size shouldBe initialDeck.size + 1

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Firecamp Attack Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(TestCards.squirrel :: TestCards.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(TestCards.bear))

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          val initialCreature = TestCards.bear
          updatedCreature.attack shouldNot be(initialCreature.attack)
          updatedCreature.withAttack(initialCreature.attack) shouldBe initialCreature

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Firecamp Health Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(TestCards.squirrel :: TestCards.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(TestCards.bear))

      When("Executing the Firecamp Health event")
      val updatedGameState = fireCamp_Health(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head
      updatedCard.health shouldNot be(TestCards.bear.health)
      updatedCard.withHealth(TestCards.bear.health) shouldBe TestCards.bear

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Mushroom Expert Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(TestCards.squirrel :: TestCards.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(TestCards.bear))

      When("Executing the Mushroom Expert event")
      val updatedGameState = mushRoomsExpert(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health and attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          val initialCreature = TestCards.bear
          updatedCard.health shouldBe (initialCreature.health * 2)
          updatedCreature.attack shouldBe (initialCreature.attack * 2)
          updatedCreature.withAttack(initialCreature.attack).withHealth(initialCreature.health) shouldBe initialCreature

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Sacrifice Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(TestCards.card1 :: TestCards.card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      // Simula la selezione di card1 e card2 lato GUI
      ch.sendToGame(GUIMessages.SingleCard(TestCards.card1))
      ch.sendToGame(GUIMessages.SingleCard(TestCards.card2))

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)

      Then("The deck size should have been changed")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)

      And("The selected cards should be removed and the new card should have the seals of the first card")
      updatedGameState.deck.toList should not contain TestCards.card1
      updatedGameState.deck.toList should not contain TestCards.card2

      val updatedCard = updatedGameState.deck.toList.find(_.name == TestCards.card2.name)

      updatedCard match {
        case Some(creature: CreatureCard) =>
          creature.seals shouldBe TestCards.card1.seals

        case Some(_) =>
          fail("The updated card is not a CreatureCard")

        case None =>
          fail("No updated card found with the expected name")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }
  }
