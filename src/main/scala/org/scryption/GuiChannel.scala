package org.scryption

import org.scryption.game.model.{BoardPosition, Card, events}

import java.util.concurrent.LinkedBlockingQueue



enum GUIMessages:
  case Cards(cards: List[Card[?]])
  case SingleCard(card: Card[?])
  ///// for fight
  case FightState(fightState: events.FightState)
  case DrawFromSquirrel
  case DrawFromDeck
  case CardToPlay(card: Card[?], position: Int)
  case CardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition])
  case EndPlayerTurn
  case End

trait GUIChannelInterface:
  def sendToGui(message: GUIMessages): Unit
  def receiveFromGui: GUIMessages
  def receiveFromGame: GUIMessages
  def sendToGame(message: GUIMessages): Unit
  def clear(): Unit


class GUIChannel private (
                           private val toGui: LinkedBlockingQueue[GUIMessages],
                           private val toGame: LinkedBlockingQueue[GUIMessages]
                         ) extends GUIChannelInterface:

  private val lock = new Object

  override def sendToGui(message: GUIMessages): Unit = lock.synchronized {
    toGui.put(message)
  }

  override def sendToGame(message: GUIMessages): Unit = lock.synchronized {
    toGame.put(message)
  }

  override def receiveFromGui: GUIMessages = toGame.take()
  override def receiveFromGame: GUIMessages = toGui.take()


  override def clear(): Unit = lock.synchronized {
    toGui.clear()
    toGame.clear()
  }

object GUIChannel:
  def getNewChannel: GUIChannelInterface =
    new GUIChannel(
      new LinkedBlockingQueue[GUIMessages](),
      new LinkedBlockingQueue[GUIMessages]()
    )
