package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.{Card, CardLibrary, CreatureCard}
import org.scryption.game.model.Deck.*

class changeDeckEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {

  /**
   * Helper per simulare il comportamento del thread GUI:
   * riceve le carte proposte dal gioco, seleziona la prima della lista,
   * la invia al gioco e attende il messaggio di fine evento.
   *
   * @return Tupla contenente il thread avviato e una funzione getter per recuperare
   *         la carta selezionata al termine dello scambio.
   */
  private def runFakeGuiThread(ch: GUIChannel.GUIChannel): (Thread, () => Card[?]) = {
    var selectedCard: Card[?] = null

    val thread = new Thread(() => {
      ch.receiveFromGame match {
        case GUIMessages.Cards(offeredCards) =>
          selectedCard = offeredCards.head
          ch.sendToGame(GUIMessages.SingleCard(selectedCard))
        case _ => ()
      }
      ch.receiveFromGame match {
        case GUIMessages.End => ()
        case _ => ()
      }
    })

    thread.start()
    (thread, () => selectedCard)
  }

  Feature("Events that change the deck") {

    Scenario("Adding a new card via GetANewCard") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(CardLibrary.squirrel :: CardLibrary.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      And("A Fake GUI running on a separate thread")
      val (guiThread, getSelectedCard) = runFakeGuiThread(ch)

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)
      val selectedCard = getSelectedCard()

      Then("The resulting deck should equal the initial deck with the chosen card added")
      updatedGameState.deck shouldBe (initialDeck addCard selectedCard)
      updatedGameState.deck.size shouldBe initialDeck.size + 1

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false

      guiThread.join(1000)
    }

    Scenario("Powering up a card with the Firecamp Attack Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(CardLibrary.squirrel :: CardLibrary.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      And("A Fake GUI running on a separate thread")
      val (guiThread, getSelectedCard) = runFakeGuiThread(ch)

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)
      val selectedCard = getSelectedCard()

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          val initialCreature = selectedCard.asInstanceOf[CreatureCard]
          updatedCreature.attack shouldNot be(initialCreature.attack)
          updatedCreature.withAttack(initialCreature.attack) shouldBe initialCreature

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false

      guiThread.join(1000)
    }

    Scenario("Powering up a card with the Firecamp Health Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(CardLibrary.squirrel :: CardLibrary.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      And("A Fake GUI running on a separate thread")
      val (guiThread, getSelectedCard) = runFakeGuiThread(ch)

      When("Executing the Firecamp Health event")
      val updatedGameState = fireCamp_Health(initialGameState, ch)
      val selectedCard = getSelectedCard()

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head
      updatedCard.health shouldNot be(selectedCard.health)
      updatedCard.withHealth(selectedCard.health) shouldBe selectedCard

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false

      guiThread.join(1000)
    }


    Scenario("Powering up a card with the Mushroom Expert Event") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(CardLibrary.squirrel :: CardLibrary.bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      And("A Fake GUI running on a separate thread")
      val (guiThread, getSelectedCard) = runFakeGuiThread(ch)

      When("Executing the Mushroom Expert event")
      val updatedGameState = mushRoomsExpert(initialGameState, ch)
      val selectedCard = getSelectedCard()

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head
      
      updatedCard match {
        case updatedCreature: CreatureCard =>
          val initialCreature = selectedCard.asInstanceOf[CreatureCard]
          updatedCard.health shouldBe (initialCreature.health * 2)
          updatedCreature.attack shouldBe (initialCreature.attack * 2)
          updatedCreature.withAttack(initialCreature.attack).withHealth(initialCreature.health) shouldBe initialCreature

        case _ =>
          fail("The updated card is not a CreatureCard")
      }
      

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false

      guiThread.join(1000)
    }

  }
}