package org.scryption.game.model.events

import org.scryption.game.model.*
import org.scryption.game.model.Deck
import org.scryption.GUIMessages
import org.scryption.GUIChannelInterface

import scala.annotation.tailrec




case class GameState(deck: Deck.Deck, isGameOver: Boolean)
type Event = (GameState, GUIChannelInterface) => GameState


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
  val cardNumbersForGui = 5
  ch.sendToGui(
    GUIMessages.Cards(gameState.deck.drawRandom(Math.min(cardNumbersForGui, gameState.deck.size), 42)._1)
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
  val cardNumbersForGui = 5

  ch.sendToGui(
    GUIMessages.Cards(
      gameState.deck.drawRandom(Math.min(cardNumbersForGui, gameState.deck.size), 42)._1
    )
  )

  val firstMessage = ch.receiveFromGui
  val secondMessage = ch.receiveFromGui

  (firstMessage, secondMessage) match {
    case (GUIMessages.SingleCard(firstCard), GUIMessages.SingleCard(secondCard)) =>
      val seals = firstCard.seals
      val updatedCard = seals.foldLeft(secondCard)((card, seal) => card.addSeal(seal))

      val updatedDeck = gameState.deck
        .removeCard(firstCard)
        .removeCard(secondCard)
        .addCard(updatedCard)
      ch.sendToGui(GUIMessages.End)
      GameState(updatedDeck, gameState.isGameOver)

    case _ =>
      ch.clear()
      sacrifice(gameState, ch)
  }
}