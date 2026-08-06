package org.scryption.game.model.events

import org.scryption.game.model.*
import org.scryption.game.model.Deck
import org.scryption.GUIMessages
import org.scryption.GUIChannelInterface

import scala.annotation.tailrec

case class GameState(deck: Deck.Deck, isGameOver: Boolean)
type Event = (GameState, GUIChannelInterface) => GameState

// Maximum number of cards sent to the GUI for selection
val cardsNumberForGui = 5

/**
 * Event: Draws 3 random cards from the library and sends them to the GUI.
 * The player picks one card to add to their deck.
 */
@tailrec
def getANewCard(gameState: GameState, ch: GUIChannelInterface): GameState = {
  ch.sendToGui(GUIMessages.Cards(CardLibrary.getADeckWithAllTheLibrary.drawRandom(3, 42)._1))
  val message = ch.receiveFromGui
 
  message match {
    case GUIMessages.SingleCard(card) =>
      ch.sendToGui(GUIMessages.End)
      GameState(gameState.deck.addCard(card), gameState.isGameOver)
    case _ =>
      ch.clear()
      getANewCard(gameState, ch)
  }
}

/**
 * Event Mushroom Expert (Mycologists):
 * Checks for duplicate cards in the deck. If present, allows the GUI to pick one:
 * removes 2 copies of that card from the deck and adds 1 fused/boosted version (stats doubled).
 * If there are no duplicates in the deck, returns the unchanged GameState immediately.
 */
@tailrec
def mushRoomsExpert(gameState: GameState, ch: GUIChannelInterface): GameState = {
  val deckList = gameState.deck.toList

  // Group cards to identify those occurring at least twice in the deck
  val duplicateCards = deckList
    .groupBy(identity)
    .filter { case (_, instances) => instances.size >= 2 }
    .keys
    .toList

  if duplicateCards.nonEmpty then
    ch.sendToGui(GUIMessages.Cards(duplicateCards.take(cardsNumberForGui)))
  else
    return gameState

  ch.receiveFromGui match {
    case GUIMessages.SingleCard(selectedCard) =>
      val upgradedCard = modifyCreature(selectedCard)(c =>
        c withAttack (c.attack * 2) withHealth (c.health * 2)
      )

      // Remove two exact copies of the selected card
      val updatedDeck = gameState.deck
        .removeCard(selectedCard)
        .removeCard(selectedCard)
        .addCard(upgradedCard)

      ch.sendToGui(GUIMessages.End)
      GameState(updatedDeck, gameState.isGameOver)

    case _ =>
      ch.clear()
      mushRoomsExpert(gameState, ch)
  }
}

/**
 * Event Firecamp (Attack):
 * Allows selecting a card from the deck to increase its attack stat by +1.
 */
def fireCamp_Attack(gameState: GameState, ch: GUIChannelInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack + 1))
  )

/**
 * Event Firecamp (Health):
 * Allows selecting a card from the deck to increase its health stat by +2.
 */
def fireCamp_Health(gameState: GameState, ch: GUIChannelInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withHealth (c.health + 2))
  )

/**
 * Event Sacrifice:
 * Allows sacrificing a card with seals to transfer all its seals onto another card in the deck.
 * If no cards in the deck have any seals, the event terminates and returns the original GameState.
 */
@tailrec
def sacrifice(gameState: GameState, ch: GUIChannelInterface): GameState = {
  val cardsWithSeals = gameState.deck.toList.filter(c => c.seals.nonEmpty)

  if cardsWithSeals.nonEmpty then
    ch.sendToGui(
      GUIMessages.Cards(
        cardsWithSeals.take(cardsNumberForGui)
      )
    )
  else
    return gameState

  ch.receiveFromGui match {
    case GUIMessages.SingleCard(cardToRemove) =>
      val deckWithoutTheCard = gameState.deck.removeCard(cardToRemove)

      ch.sendToGui(
        GUIMessages.Cards(
          deckWithoutTheCard.drawRandom(Math.min(cardsNumberForGui, deckWithoutTheCard.size), 42)._1
        )
      )

      ch.receiveFromGui match {
        case GUIMessages.SingleCard(cardToUpgrade) =>
          val seals = cardToRemove.seals
          val updatedCard = seals.foldLeft(cardToUpgrade)((card, seal) => card.addSeal(seal))

          val updatedDeck = gameState.deck
            .removeCard(cardToRemove)
            .removeCard(cardToUpgrade)
            .addCard(updatedCard)

          ch.sendToGui(GUIMessages.End)
          GameState(updatedDeck, gameState.isGameOver)

        case _ =>
          ch.clear()
          sacrifice(gameState, ch)
      }

    case _ =>
      ch.clear()
      sacrifice(gameState, ch)
  }
}

/**
 * Generic helper function for events that replace a card in the deck
 * with a modified version produced by transformation `f`.
 */
@tailrec
private def substituteACard(gameState: GameState, ch: GUIChannelInterface, f: Card[?] => Card[?]): GameState = {
  ch.sendToGui(
    GUIMessages.Cards(gameState.deck.drawRandom(Math.min(cardsNumberForGui, gameState.deck.size), 42)._1)
  )
  val message = ch.receiveFromGui

  message match {
    case GUIMessages.SingleCard(card) =>
      ch.sendToGui(GUIMessages.End)
      val updatedDeck = gameState.deck removeCard card addCard f(card)
      GameState(updatedDeck, gameState.isGameOver)
    case _ =>
      ch.clear()
      substituteACard(gameState, ch, f)
  }
}

/**
 * Helper to modify card attributes only if the card is a CreatureCard.
 */
private def modifyCreature(card: Card[?])(f: CreatureCard => Card[?]): Card[?] = card match {
  case c: CreatureCard => f(c)
  case other           => other
}