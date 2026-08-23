package org.scryption

import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.game.model.items.GameItem
import org.scryption.game.model.{BoardPosition, Card}

import java.util.concurrent.LinkedBlockingQueue

sealed trait GameMessage

enum Trial:
  case Health
  case Attack
  case Seals

enum EventMessages extends GameMessage:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  case TrialChoice(trial: Trial)
  case Items(items: List[GameItem])
  case SingleItem(item:GameItem)
  case End



enum FightMessages extends GameMessage:
  case State(fightState: FightState, turn: TurnState)
  case DrawFromSquirrel
  case DrawFromDeck
  case CardToPlay(card: Card[?], position: Int)
  case CardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition])
  case UseItem(item: GameItem, target: Option[BoardPosition] = None)
  case EndPlayerTurn
  case End


enum MapMessages extends GameMessage:
  case left
  case right
  case forward

trait GameMessagesChannel:
  def sendToGui(message: GameMessage): Unit
  def receiveFromGui: GameMessage
  def receiveFromGame: GameMessage
  def sendToGame(message: GameMessage): Unit
  def clear(): Unit


class GameMessagesChannelImpl (
                           private val toGui: LinkedBlockingQueue[GameMessage],
                           private val toGame: LinkedBlockingQueue[GameMessage]
                         ) extends GameMessagesChannel:

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

object GameMessagesChannel:
  def apply(): GameMessagesChannel =
    new GameMessagesChannelImpl(
      new LinkedBlockingQueue[GameMessage](),
      new LinkedBlockingQueue[GameMessage]()
    )
