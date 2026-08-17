package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.{Deck, GameState}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.{GUIChannel, GUIChannelInterface}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  private type GameEvent = (Event, GUIChannelInterface => Panel)

  private val getANewCardEvent: GameEvent     = (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch))
  private val fightEvent: GameEvent           = (fight, (ch: GUIChannelInterface) => new FightView(channel = ch))
  private val fireCampAttackEvent: GameEvent  = (fireCamp_Attack, (ch: GUIChannelInterface) => new FireCampAttackView(channel = ch))
  private val fireCampHealthEvent: GameEvent  = (fireCamp_Health, (ch: GUIChannelInterface) => new FireCampHealthView(channel = ch))
  private val mycologistsEvent: GameEvent     = (mushRoomsExpert, (ch: GUIChannelInterface) => new MycologistsView(channel = ch))
  private val sacrificeEvent: GameEvent       = (sacrifice, (ch: GUIChannelInterface) => new StrangeStonesView(channel = ch))

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState

      val listEvents: List[GameEvent] = List(
        getANewCardEvent,
        fightEvent,
        fireCampAttackEvent,
        fireCampHealthEvent,
        mycologistsEvent,
        sacrificeEvent
      )

      Future {
        try
          gameLoop(initialState, listEvents)
        finally
          running = false
          onGameOver()
      }

  @tailrec
  private def gameLoop(gameState: GameState, events: List[GameEvent]): Unit =
    (gameState, events) match
      case (_, Nil) | (GameState(_, true), _) =>
        println(s"=== PARTITA TERMINATA ===")
        println(s"Mazzo Finale: ${gameState.deck}")
      case (_, (event, createView) :: remainingEvents) =>
        val ch = GUIChannel.getNewChannel
        onViewChange(createView(ch))
        val nextState = event(gameState, ch)
        gameLoop(nextState, remainingEvents)