package org.scryption.view

import org.scryption.game.model.Card
import org.scryption.game.model.BoardPosition
import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.view.common.{CardViewInfo, toViewInfo}
import org.scryption.{FightMessages, GUIChannelInterface}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewModelFight(channel: GUIChannelInterface):

  def listenForUpdatedState(onUpdate: (FightState, TurnState) => Unit): Unit =
    Future {
      while true do
        channel.receiveFromGame match
          case FightMessages.State(fightState, turn) => onUpdate(fightState, turn)
          case _ =>
    }

  def cardToPlay(card: Card[?], position: Int): Unit =
    channel.sendToGame(FightMessages.CardToPlay(card, position))

  def cardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition]): Unit =
    channel.sendToGame(FightMessages.CardToPlayWithSacrifices(card, position, sacrificesPositions))
    
    
  def drawFromSquirrel() : Unit =
    channel.sendToGame(FightMessages.DrawFromSquirrel)
    
  def drawFromDeck() : Unit =
    channel.sendToGame(FightMessages.DrawFromDeck)
    
  def endTurn(): Unit =
    channel.sendToGame(FightMessages.EndPlayerTurn)
