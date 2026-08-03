package org.scryption.game.model.events

import org.scryption.game.model.*
import org.scryption.game.model.Deck
import org.scryption.GUIMessages
import org.scryption.GUIChannelInterface

import scala.annotation.tailrec




case class GameState(deck: Deck.Deck, isGameOver: Boolean)
type Event = (GameState, GUIChannelInterface) => GameState
val cardsNumberForGui = 5

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

@tailrec
def substituteACard(gameState: GameState, ch: GUIChannelInterface, f: Card[?] => Card[?]): GameState = {

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

private def modifyCreature(card: Card[?])(f: CreatureCard => Card[?]): Card[?] = card match {
  case c: CreatureCard => f(c)
  case other           => other
}

def mushRoomsExpert(gameState: GameState, ch: GUIChannelInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack * 2) withHealth (c.health * 2))
  )

def fireCamp_Attack(gameState: GameState, ch: GUIChannelInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack + 1))
  )

def fireCamp_Health(gameState: GameState, ch: GUIChannelInterface): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withHealth (c.health + 2))
  )

@tailrec
def sacrifice(gameState: GameState, ch: GUIChannelInterface): GameState = {


  val cardsWithSeals = gameState.deck.toList.filter(c => c.seals.nonEmpty)
  
  if cardsWithSeals.nonEmpty then 
    ch.sendToGui(
      GUIMessages.Cards(
        cardsWithSeals.take(cardsNumberForGui)
      )
    )
  else return gameState  

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