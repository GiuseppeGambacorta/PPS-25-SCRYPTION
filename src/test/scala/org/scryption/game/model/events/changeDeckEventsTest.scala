package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.{Card, CreatureCard, Seal}
import org.scryption.game.model.Deck.*
import org.scryption.game.model.Seal.{Sprinter, Wall}
import org.scryption.GUIMessages
import org.scryption.GUIChannel
import org.scryption.GUIChannelInterface

object TestCards:
  val squirrel: CreatureCard = CreatureCard.empty withAttack(1) named("squirrel") withHealth(1)
  val bear: CreatureCard = CreatureCard.empty withAttack(2) named("bear") withHealth(3)
  val fox: CreatureCard = CreatureCard.empty withAttack(1) named("fox") withHealth(1)

  val card1: CreatureCard = CreatureCard.empty withAttack(0) named("firstCard") withHealth(1) addSeal(Sprinter) addSeal(Wall)
  val card2: CreatureCard = CreatureCard.empty withAttack(0) named("secondCard") withHealth(1)

class ChangeDeckEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private val squirrel = TestCards.squirrel
  private val bear = TestCards.bear
  private val fox = TestCards.fox
  private val card1 = TestCards.card1
  private val card2 = TestCards.card2

  /**
   * Helper per simulare un thread GUI che risponde prima in modo errato
   * e poi in modo corretto dopo il retry dell'evento.
   */
  private def runMockGuiWithRetry(ch: GUIChannelInterface)(wrongResponses: List[GUIMessages], correctResponses: List[GUIMessages]): Thread =
    val thread = new Thread(() => {
      
      ch.receiveFromGame
      wrongResponses.foreach(ch.sendToGame)
      
      ch.receiveFromGame
      correctResponses.foreach(ch.sendToGame)
    })
    thread.start()
    thread

  Feature("Events that change the deck") {

    Scenario("Adding a new card via GetANewCard") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(squirrel))

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)

      Then("The resulting deck should equal the initial deck with the chosen card added")
      updatedGameState.deck shouldBe (initialDeck addCard squirrel)
      updatedGameState.deck.size shouldBe initialDeck.size + 1

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Firecamp Attack Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          updatedCreature.attack shouldNot be(bear.attack)
          updatedCreature.withAttack(bear.attack) shouldBe bear

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Firecamp Health Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Firecamp Health event")
      val updatedGameState = fireCamp_Health(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head
      updatedCard.health shouldNot be(bear.health)
      updatedCard.withHealth(bear.health) shouldBe bear

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Mushroom Expert Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Mushroom Expert event")
      val updatedGameState = mushRoomsExpert(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health and attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          updatedCard.health shouldBe (bear.health * 2)
          updatedCreature.attack shouldBe (bear.attack * 2)
          updatedCreature.withAttack(bear.attack).withHealth(bear.health) shouldBe bear

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Powering up a card with the Sacrifice Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(card1 :: card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      ch.sendToGame(GUIMessages.SingleCard(card1))
      ch.sendToGame(GUIMessages.SingleCard(card2))

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)

      Then("The deck size should have been changed")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)

      And("The selected cards should be removed and the new card should have the seals of the first card")
      updatedGameState.deck.toList should not contain card1
      updatedGameState.deck.toList should not contain card2

      val updatedCard = updatedGameState.deck.toList.find(_.name == card2.name)

      updatedCard match {
        case Some(creature: CreatureCard) =>
          creature.seals shouldBe card1.seals

        case Some(_) =>
          fail("The updated card is not a CreatureCard")

        case None =>
          fail("No updated card found with the expected name")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }
  }

  Feature("Handling invalid messages and retries") {

    Scenario("GetANewCard receives an unexpected Cards message before SingleCard") {
      Given("A mock GUI thread that sends Cards first, and SingleCard after retry")
      val initialDeck = fromList(squirrel :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.Cards(List(fox))),
        correctResponses = List(GUIMessages.SingleCard(squirrel))
      )

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)
      guiThread.join()

      Then("It should recover from the invalid message, clear the channel, and apply the valid card")
      updatedGameState.deck.size shouldBe initialDeck.size + 1
      updatedGameState.deck.toList should contain(squirrel)
    }

    Scenario("Substitute card (Firecamp Attack) receives GUIMessages.End before SingleCard") {
      Given("A mock GUI thread that sends End first, and SingleCard after retry")
      val initialDeck = fromList(bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.End),
        correctResponses = List(GUIMessages.SingleCard(bear))
      )

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)
      guiThread.join()

      Then("The event should recover and process the valid card choice")
      updatedGameState.deck.size shouldBe initialDeck.size
      val updatedCard = updatedGameState.deck.toList.head
      updatedCard.asInstanceOf[CreatureCard].attack shouldBe (bear.attack + 1)
    }

    Scenario("Sacrifice receives an invalid message sequence before a valid pair") {
      Given("A mock GUI thread that sends SingleCard+Cards first, and two SingleCards after retry")
      val initialDeck = fromList(card1 :: card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.SingleCard(card1), GUIMessages.Cards(List(squirrel))),
        correctResponses = List(GUIMessages.SingleCard(card1), GUIMessages.SingleCard(card2))
      )

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)
      guiThread.join()

      Then("The sacrifice event should recover after the retry and combine the seals correctly")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)
      val updatedCard = updatedGameState.deck.toList.find(_.name == card2.name)
      updatedCard.get.asInstanceOf[CreatureCard].seals shouldBe card1.seals
    }
  }