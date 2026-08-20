package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.Deck.*
import org.scryption.game.model.Seal.{MightyLeap, Sprinter}
import org.scryption.game.model.*
import org.scryption.game.model.items.{HoggyBank, Pliers, Scissors, SquirrelBottle, allItems}
import org.scryption.{EventMessages, GameMessagesChannel}

class ItemsEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private def runMockGuiWithRetry(ch: GameMessagesChannel)(wrongResponses: List[EventMessages], correctResponses: List[EventMessages]): Thread =
    val thread = new Thread(() => {
      ch.receiveFromGame
      wrongResponses.foreach(ch.sendToGame)

      ch.receiveFromGame
      correctResponses.foreach(ch.sendToGame)
    })
    thread.start()
    thread

  Feature("GetANewItem Event") {

    Scenario("Successfully adding a new item when receiving a valid SingleItem message") {
      Given("An initial GameState with no items and a GameMessageChannel With a SingleItem message")
      val initialGameState = GameState.getInitialGameState
      val ch = GameMessagesChannel()
      ch.sendToGame(EventMessages.SingleItem(SquirrelBottle()))

      When("Executing the GetANewItem event")
      val updatedGameState = getANewItemEvent(initialGameState, ch)

      Then("The resulting inventory should equal the initial inventory with the chosen item added")
      updatedGameState.inventory shouldBe (initialGameState.inventory :+ SquirrelBottle())
      updatedGameState.inventory.size shouldBe initialGameState.inventory.size + 1

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Handling an unexpected Cards message before receiving a valid SingleItem") {
      Given("A mock GUI thread that sends Items first, and a Single Item after retry")
      val initialGameState = GameState.getInitialGameState
      val ch = GameMessagesChannel()

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(EventMessages.Items(allItems)),
        correctResponses = List(EventMessages.SingleItem(SquirrelBottle()))
      )

      When("Executing the GetANewItem event")
      val updatedGameState = getANewItemEvent(initialGameState, ch)
      guiThread.join()

      Then("It should recover from the invalid message, clear the channel, and apply the valid item")
      updatedGameState.inventory.size shouldBe initialGameState.inventory.size + 1
      updatedGameState.inventory should contain(SquirrelBottle())
    }

    Scenario("Receiving a SingleCard instead when inventory is full (size >= 4)") {
      Given("A GameState with 4 items in inventory and a channel providing SingleCard confirmation")
      val fullInventory = List(SquirrelBottle(), HoggyBank(), Pliers(), Scissors())
      val initialGameState = GameState.getInitialGameState.copy(inventory = fullInventory)
      val cardToAdd = CardLibrary.wolf
      val ch = GameMessagesChannel()

      // Il mock GUI riceve il SingleCard generato dal model e risponde confermando la carta
      val guiThread = new Thread(() => {
        val received = ch.receiveFromGame
        received shouldBe a[EventMessages.SingleCard]
        ch.sendToGame(EventMessages.SingleCard(cardToAdd))
      })
      guiThread.start()

      When("Executing the GetANewItem event")
      val updatedGameState = getANewItemEvent(initialGameState, ch)
      guiThread.join()

      Then("The inventory size should remain unchanged (4 items)")
      updatedGameState.inventory shouldBe fullInventory
      updatedGameState.inventory.size shouldBe 4

      And("The chosen card should be added to the deck")
      updatedGameState.deck.size shouldBe initialGameState.deck.size + 1
      updatedGameState.deck.toList should contain(cardToAdd)

      And("The GUI channel should have received End")
      ch.receiveFromGame shouldBe EventMessages.End
    }

    Scenario("Handling an unexpected message when inventory is full before receiving a valid SingleCard") {
      Given("A GameState with 4 items and a mock GUI that sends an invalid message first")
      val fullInventory = List(SquirrelBottle(), HoggyBank(), Pliers(), Scissors())
      val initialGameState = GameState.getInitialGameState.copy(inventory = fullInventory)
      val cardToAdd = CardLibrary.wolf
      val ch = GameMessagesChannel()

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(EventMessages.Items(allItems)),
        correctResponses = List(EventMessages.SingleCard(cardToAdd))
      )

      When("Executing the GetANewItem event with full inventory")
      val updatedGameState = getANewItemEvent(initialGameState, ch)
      guiThread.join()

      Then("It should retry, keep inventory intact, and successfully add the card to the deck")
      updatedGameState.inventory.size shouldBe 4
      updatedGameState.deck.toList should contain(cardToAdd)
    }
  }