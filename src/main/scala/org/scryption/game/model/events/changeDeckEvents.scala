package org.scryption.game.model.events

import java.util.concurrent.LinkedBlockingQueue
import org.scryption.game.model.*
import org.scryption.game.model.Deck
import GUIChannel.*

import scala.annotation.tailrec

trait GUIChannelInterface:
  def sendToGui(message: GUIMessages): Unit
  def receiveFromGui: GUIMessages
  def receiveFromGame: GUIMessages
  def sendToGame(message: GUIMessages): Unit

class GUIChannel private (
                           private val toGui: LinkedBlockingQueue[GUIMessages],
                           private val toGame: LinkedBlockingQueue[GUIMessages]
                         ) extends GUIChannelInterface:

  // Metodi usati dal thread del gioco
  override def sendToGui(message: GUIMessages): Unit = toGui.put(message)
  override def receiveFromGui: GUIMessages = toGame.take()

  // Metodi usati dal thread della GUI
  override def receiveFromGame: GUIMessages = toGui.take()
  override def sendToGame(message: GUIMessages): Unit = toGame.put(message)

object GUIChannel:
  def getNewChannel: GUIChannelInterface =
    new GUIChannel(
      new LinkedBlockingQueue[GUIMessages](),
      new LinkedBlockingQueue[GUIMessages]()
    )

case class GameState(deck: Deck.Deck, isGameOver: Boolean)

// Usa l'interfaccia per rendere il modello disaccoppiato dall'implementazione
type Event = (GameState, GUIChannelInterface) => GameState

enum GUIMessages:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  case End

@tailrec
def getANewCard(gameState: GameState, ch: GUIChannelInterface): GameState = {
  ch.sendToGui(GUIMessages.Cards(CardLibrary.getADeckWithAllTheLibrary.drawRandom(3, 42)._1))
  val message = ch.receiveFromGui

  message match {
    case GUIMessages.SingleCard(card) =>
      ch.sendToGui(GUIMessages.End)
      GameState(gameState.deck.addCard(card), gameState.isGameOver)
    case _ =>
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
      sacrifice(gameState, ch)
  }
}