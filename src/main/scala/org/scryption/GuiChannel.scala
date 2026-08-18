package org.scryption

import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.game.model.{BoardPosition, Card}

import java.util.concurrent.LinkedBlockingQueue

sealed trait GameMessage

enum EventMessages extends GameMessage:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  case End

enum FightMessages extends GameMessage:
  case State(fightState: FightState, turn: TurnState)
  case DrawFromSquirrel
  case DrawFromDeck
  case CardToPlay(card: Card[?], position: Int)
  case CardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition])
  case EndPlayerTurn
  case End

trait GUIChannelInterface:
  def sendToGui(message: GameMessage): Unit
  def receiveFromGui: GameMessage
  def receiveFromGame: GameMessage
  def sendToGame(message: GameMessage): Unit
  def clear(): Unit


class GUIChannel private (
                           private val toGui: LinkedBlockingQueue[GameMessage],
                           private val toGame: LinkedBlockingQueue[GameMessage]
                         ) extends GUIChannelInterface:

  private val lock = new Object

  override def sendToGui(message: GameMessage): Unit = lock.synchronized {
    toGui.put(message)
  }

  override def sendToGame(message: GameMessage): Unit = lock.synchronized {
    toGame.put(message)
  }

  override def receiveFromGui: GameMessage = toGame.take()
  override def receiveFromGame: GameMessage = toGui.take()


  override def clear(): Unit = lock.synchronized {
    toGui.clear()
    toGame.clear()
  }

object GUIChannel:
  def getNewChannel: GUIChannelInterface =
    new GUIChannel(
      new LinkedBlockingQueue[GameMessage](),
      new LinkedBlockingQueue[GameMessage]()
    )
