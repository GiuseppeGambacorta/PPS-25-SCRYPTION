package org.scryption

import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.game.model.{BoardPosition, Card, events}

import java.util.concurrent.LinkedBlockingQueue

sealed trait CardMessage

enum EventMessages extends CardMessage:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  case End

enum FightMessages extends CardMessage:
  case State(fightState: FightState, turn: TurnState)
  case DrawFromSquirrel
  case DrawFromDeck
  case CardToPlay(card: Card[?], position: Int)
  case CardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition])
  case EndPlayerTurn
  case End

trait GUIChannelInterface:
  def sendToGui(message: CardMessage): Unit
  def receiveFromGui: CardMessage
  def receiveFromGame: CardMessage
  def sendToGame(message: CardMessage): Unit
  def clear(): Unit


class GUIChannel private (
                           private val toGui: LinkedBlockingQueue[CardMessage],
                           private val toGame: LinkedBlockingQueue[CardMessage]
                         ) extends GUIChannelInterface:

  private val lock = new Object

  override def sendToGui(message: CardMessage): Unit = lock.synchronized {
    toGui.put(message)
  }

  override def sendToGame(message: CardMessage): Unit = lock.synchronized {
    toGame.put(message)
  }

  override def receiveFromGui: CardMessage = toGame.take()
  override def receiveFromGame: CardMessage = toGui.take()


  override def clear(): Unit = lock.synchronized {
    toGui.clear()
    toGame.clear()
  }

object GUIChannel:
  def getNewChannel: GUIChannelInterface =
    new GUIChannel(
      new LinkedBlockingQueue[CardMessage](),
      new LinkedBlockingQueue[CardMessage]()
    )
