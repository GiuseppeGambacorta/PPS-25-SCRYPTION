package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.{GUIChannel, GUIChannelInterface}
import org.scryption.game.model.Maps
import org.scryption.game.model.Maps.Path
import org.scryption.game.model.Maps.Path.*
import org.scryption.game.model.events.*
import org.scryption.view.events.CardSelectionView

class MapTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private val gameState = GameState(deck = Deck.getStandardDeck, isGameOver = false)
  private val ch = GUIChannel.getNewChannel
  //private val newCard1: Event = (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch))

  Feature("Creation of a new Map from a List of Events") {

    Scenario("The Map must be End") {
      Given("an empty Event List")
      val events: List[Event] = List.empty

      When("creating a new Map")
      val map: Path[Event] = createFromList(events)

      Then("The resulting should be End")
      map.shouldBe(End())
    }

    Scenario("The Map must be Node") {
      Given("a List with one Event")
      val events: List[Event] = List(
        (gameState, ch) => getANewCard(gameState, ch)
      )

      When("creating a new Map")
      val map: Path[Event] = createFromList(events)

      Then("The result should be Node")
      map.shouldBe(Node)
    }
    
    
  }