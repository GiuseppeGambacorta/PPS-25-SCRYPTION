package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.CardLibrary
import org.scryption.game.model.Deck.*

class changeDeckEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks {


  Feature("Events that change the deck") {
    
      Scenario("Adding a new card via GetANewCard") {
        Given("An initial GameState with 2 cards and a GUIChannel")
        val initialDeck = getDeckFromList(CardLibrary.squirrel :: CardLibrary.bear :: Nil)
        val initialGameState = GameState(initialDeck, isGameOver = false)
        val ch = GUIChannel.getNewChannel
        var selectedCard = CardLibrary.squirrel

        And("A Fake GUI running on a separate thread that selects the first random card from the event")
        val guiThread = new Thread(() => {
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
        guiThread.start()

        When("Executing the GetANewCard event")
        val updatedGameState = getANewCard(initialGameState, ch)

        Then("The resulting deck should equal the initial deck with the chosen card added")
        updatedGameState.deck shouldBe initialDeck.addCard(selectedCard)
        updatedGameState.isGameOver shouldBe false

        guiThread.join(1000)
      }


   


  }
}
