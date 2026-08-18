package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.GameState
import org.scryption.view.{ViewModelEvent, ViewModelFight}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.{GUIChannel, GUIChannelInterface}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  private type GameEvent = (Event, GUIChannelInterface => Panel)

  private val getANewCard: GameEvent     = (getANewCardEvent, (ch: GUIChannelInterface) => new CardSelectionView(ViewModelEvent(ch)))
  private val fight: GameEvent           = (fightEvent, (ch: GUIChannelInterface) => new FightView(ViewModelFight(ch)))
  private val fireCampAttack: GameEvent  = (fireCampEvent_Attack, (ch: GUIChannelInterface) => new FireCampAttackView(ViewModelEvent(ch)))
  private val fireCampHealth: GameEvent  = (fireCampEvent_Health, (ch: GUIChannelInterface) => new FireCampHealthView(ViewModelEvent(ch)))
  private val mycologists: GameEvent     = (mushRoomsExpertEvent, (ch: GUIChannelInterface) => new MycologistsView(ViewModelEvent(ch)))
  private val sacrifice: GameEvent       = (sacrificeEvent, (ch: GUIChannelInterface) => new StrangeStonesView(ViewModelEvent(ch)))

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState

      val listEvents: List[GameEvent] = List(
        getANewCard,
        fight,
        fireCampAttack,
        fireCampHealth,
        mycologists,
        sacrifice
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
      case (_, Nil) | (GameState(_, _, true), _) =>
        println(s"=== PARTITA TERMINATA ===")
        println(s"Mazzo Finale: ${gameState.deck}")
      case (_, (event, createView) :: remainingEvents) =>
        val ch = GUIChannel.getNewChannel
        onViewChange(createView(ch))
        val nextState = event(gameState, ch)
        gameLoop(nextState, remainingEvents)