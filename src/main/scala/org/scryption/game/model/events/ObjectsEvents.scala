package org.scryption.game.model.events

import org.scryption.{EventMessages, GameMessagesInterface}
import org.scryption.game.model.*
import org.scryption.game.model.items.allItems
import scala.annotation.tailrec
import scala.util.Random

/**
 * Event Item / Pack:
 * - If inventory has room (< 4), offers 2 random items to pick from.
 * - Otherwise (inventory is full), grants 1 random card directly from the library.
 */
@tailrec
def getANewItemEvent(gameState: GameState, ch: GameMessagesInterface): GameState = {
  if gameState.inventory.size < 4 then
    ch.sendToGui(EventMessages.Items(Random.shuffle(allItems).take(2)))
    val message = ch.receiveFromGui

    message match {
      case EventMessages.SingleItem(item) =>
        ch.sendToGui(EventMessages.End)
        gameState.copy(inventory = gameState.inventory :+ item)
      case _ =>
        ch.clear()
        getANewItemEvent(gameState, ch)
    }
  else
    
    val (randomCards, _) = CardLibrary.getADeckWithAllTheLibrary.drawRandom(1)
    val randomCard = randomCards.headOption.getOrElse(CardLibrary.squirrel)

    ch.sendToGui(EventMessages.SingleCard(randomCard))
    val message = ch.receiveFromGui

    message match {
      case EventMessages.SingleCard(card) =>
        ch.sendToGui(EventMessages.End)
        gameState.copy(deck = gameState.deck.addCard(card))
      case _ =>
        ch.clear()
        getANewItemEvent(gameState, ch)
    }
}