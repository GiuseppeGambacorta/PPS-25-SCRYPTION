package org.scryption.game.model.events

import org.scryption.EventMessages
import org.scryption.GameMessagesInterface
import org.scryption.game.model.*
import org.scryption.game.model.Deck

import scala.annotation.tailrec

type Event = (GameState, GameMessagesInterface) => GameState

// Maximum number of cards sent to the GUI for selection
val cardsNumberForGui = 5

/**
 * Event: Draws 3 random cards from the library and sends them to the GUI.
 * The player picks one card to add to their deck.
 */
@tailrec
def getANewCardEvent(gameState: GameState, ch: GameMessagesInterface): GameState = {
  ch.sendToGui(EventMessages.Cards(CardLibrary.getADeckWithAllTheLibrary.drawRandom(3)._1))
  val message = ch.receiveFromGui

  message match {
    case EventMessages.SingleCard(card) =>
      ch.sendToGui(EventMessages.End)
      gameState.copy(deck = gameState.deck.addCard(card))
    case _ =>
      ch.clear()
      getANewCardEvent(gameState, ch)
  }
}

/**
 * Event Mushroom Expert (Mycologists):
 * Checks for duplicate cards in the deck. If present, allows the GUI to pick one:
 * removes 2 copies of that card from the deck and adds 1 fused/boosted version (stats doubled).
 * If there are no duplicates in the deck, returns the unchanged GameState immediately.
 */
@tailrec
def mushRoomsExpertEvent(gameState: GameState, ch: GameMessagesInterface): GameState = {
  val deckList = gameState.deck.toList

  // Group cards to identify those occurring at least twice in the deck
  val duplicateCards = deckList
    .groupBy(identity)
    .filter { case (_, instances) => instances.size >= 2 }
    .keys
    .toList

  if duplicateCards.nonEmpty then
    ch.sendToGui(EventMessages.Cards(duplicateCards.take(cardsNumberForGui)))
  else
    return gameState

  ch.receiveFromGui match {
    case EventMessages.SingleCard(selectedCard) =>
      val upgradedCard = modifyCreature(selectedCard)(c =>
        c withAttack (c.attack * 2) withHealth (c.health * 2)
      )

      // Remove two exact copies of the selected card
      val updatedDeck = gameState.deck
        .removeCard(selectedCard)
        .removeCard(selectedCard)
        .addCard(upgradedCard)

      ch.sendToGui(EventMessages.End)
      gameState.copy(deck = updatedDeck)

    case _ =>
      ch.clear()
      mushRoomsExpertEvent(gameState, ch)
  }
}

/**
 * Event Firecamp (Attack):
 * Allows selecting a card from the deck to increase its attack stat by +1.
 */
def fireCampEvent_Attack(gameState: GameState, ch: GameMessagesInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack + 1))
  )

/**
 * Event Firecamp (Health):
 * Allows selecting a card from the deck to increase its health stat by +2.
 */
def fireCampEvent_Health(gameState: GameState, ch: GameMessagesInterface): GameState =
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
def sacrificeEvent(gameState: GameState, ch: GameMessagesInterface): GameState = {
  val cardsWithSeals = gameState.deck.toList.filter(c => c.seals.nonEmpty)

  if cardsWithSeals.nonEmpty then
    ch.sendToGui(
      EventMessages.Cards(
        cardsWithSeals.take(cardsNumberForGui)
      )
    )
  else
    return gameState

  ch.receiveFromGui match {
    case EventMessages.SingleCard(cardToRemove) =>
      val deckWithoutTheCard = gameState.deck.removeCard(cardToRemove)

      ch.sendToGui(
        EventMessages.Cards(
          deckWithoutTheCard.drawRandom(Math.min(cardsNumberForGui, deckWithoutTheCard.size), 42)._1
        )
      )

      ch.receiveFromGui match {
        case EventMessages.SingleCard(cardToUpgrade) =>
          val seals = cardToRemove.seals
          val updatedCard = seals.foldLeft(cardToUpgrade)((card, seal) => card.addSeal(seal))

          val updatedDeck = gameState.deck
            .removeCard(cardToRemove)
            .removeCard(cardToUpgrade)
            .addCard(updatedCard)

          ch.sendToGui(EventMessages.End)
          gameState.copy(deck = updatedDeck)

        case _ =>
          ch.clear()
          sacrificeEvent(gameState, ch)
      }

    case _ =>
      ch.clear()
      sacrificeEvent(gameState, ch)
  }
}

/**
 * Generic helper function for events that replace a card in the deck
 * with a modified version produced by transformation `f`.
 */
@tailrec
private def substituteACard(gameState: GameState, ch: GameMessagesInterface, f: Card[?] => Card[?]): GameState = {
  ch.sendToGui(
    EventMessages.Cards(gameState.deck.drawRandom(Math.min(cardsNumberForGui, gameState.deck.size), 42)._1)
  )
  val message = ch.receiveFromGui

  message match {
    case EventMessages.SingleCard(card) =>
      ch.sendToGui(EventMessages.End)
      val updatedDeck = gameState.deck removeCard card addCard f(card)
      gameState.copy(deck = updatedDeck)
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