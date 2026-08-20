package org.scryption.view

import org.scryption.game.model.Card
import org.scryption.game.model.BoardPosition
import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.{FightMessages, GameMessagesChannel}

import javax.swing.Timer
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Swing

class ViewModelFight(channel: GameMessagesChannel):

  private val eventQueue = mutable.Queue.empty[(FightState, TurnState)]
  private var isPlayingBack = false
  private var viewUpdateCallback: Option[(FightState, TurnState) => Unit] = None

  def listenForUpdatedState(onUpdate: (FightState, TurnState) => Unit): Unit =
    viewUpdateCallback = Some(onUpdate)
    Future {
      while true do
        channel.receiveFromGame match
          case FightMessages.State(fightState, turn) =>
            Swing.onEDT {
              eventQueue.enqueue((fightState, turn))
              if !isPlayingBack then processNextEvent()
            }
          case _ =>
    }

  private def processNextEvent(): Unit =
    if eventQueue.isEmpty then
      isPlayingBack = false
    else
      isPlayingBack = true
      val (state, turn) = eventQueue.dequeue()
      viewUpdateCallback.foreach(_(state, turn))
      val delayMs = turn match
        case TurnState.playerFight => 2000
        case TurnState.botTurn     => 2000
        case TurnState.botFight    => 2000
        case _                     => 0
      if delayMs > 0 then
        val timer = new Timer(delayMs, _ => processNextEvent())
        timer.setRepeats(false)
        timer.start()
      else
        processNextEvent()

  def calculateBlood(board: Board, sacrificesPositions: List[BoardPosition]): Int =
    SacrificeManager().resolveSacrifices(board, sacrificesPositions).generatedBlood

  def cardToPlay(card: Card[?], position: Int): Unit =
    channel.sendToGame(FightMessages.CardToPlay(card, position))

  def cardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition]): Unit =
    channel.sendToGame(FightMessages.CardToPlayWithSacrifices(card, position, sacrificesPositions))

  def drawFromSquirrel() : Unit =
    channel.sendToGame(FightMessages.DrawFromSquirrel)

  def useItem(item: org.scryption.game.model.items.GameItem, target: Option[BoardPosition] = None): Unit =
    channel.sendToGame(FightMessages.UseItem(item, target))
    
  def drawFromDeck() : Unit =
    channel.sendToGame(FightMessages.DrawFromDeck)
    
  def endTurn(): Unit =
    channel.sendToGame(FightMessages.EndPlayerTurn)
