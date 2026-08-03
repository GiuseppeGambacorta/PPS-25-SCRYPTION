package org.scryption.game.model.events

import java.util.concurrent.LinkedBlockingQueue
import org.scryption.game.model.*
import org.scryption.game.model.Deck
import GUIChannel.*

import scala.annotation.tailrec



object GUIChannel:

  opaque type GUIChannel = (LinkedBlockingQueue[GUIMessages], LinkedBlockingQueue[GUIMessages])

  def getNewChannel: GUIChannel =
    (new LinkedBlockingQueue[GUIMessages](), new LinkedBlockingQueue[GUIMessages]())

  extension (ch: GUIChannel)
    // --- Metodi usati dal Thread del Gioco ---
    def sendToGui(message: GUIMessages): Unit = ch._1.put(message)
    def receiveFromGui: GUIMessages = ch._2.take() // Attende bloccando senza eccezioni

    // --- Metodi usati dal Thread della GUI ---
    def receiveFromGame: GUIMessages = ch._1.take() // Attende bloccando senza eccezioni
    def sendToGame(message: GUIMessages): Unit = ch._2.put(message)

case class GameState(deck: Deck.Deck, isGameOver: Boolean)
type Event = (GameState, GUIChannel) => GameState

enum GUIMessages:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  case End




@tailrec
def getANewCard(gameState: GameState, ch: GUIChannel): GameState = {
  ch.sendToGui(GUIMessages.Cards(CardLibrary.getADeckWithAllTheLibrary.drawRandom(3,42)._1))
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
def substituteACard(gameState: GameState, ch: GUIChannel, f: Card[?] => Card[?]): GameState = {
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

def mushRoomsExpert(gameState: GameState, ch: GUIChannel): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack * 2) withHealth (c.health * 2))
  )

def fireCamp_Attack(gameState: GameState, ch: GUIChannel): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack + 1))
  )

def fireCamp_Health(gameState: GameState, ch: GUIChannel): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withHealth (c.health + 2))
  )



@tailrec
def sacrifice(gameState: GameState, ch: GUIChannel): GameState = {
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