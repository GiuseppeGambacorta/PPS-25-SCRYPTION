package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.GameState
import org.scryption.view.{ViewModelDeckEvent, ViewModelFight, ViewModelItemEvent}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.{GameMessagesChannel, GameMessagesInterface}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  private type GameEvent = (Event, GameMessagesInterface => Panel)

  private val getANewCard: GameEvent     = (getANewCardEvent, (ch: GameMessagesInterface) => new CardSelectionView(ViewModelDeckEvent(ch)))
  private val fight: GameEvent           = (fightEvent, (ch: GameMessagesInterface) => new FightView(ViewModelFight(ch)))
  private val fireCampAttack: GameEvent  = (fireCampEvent_Attack, (ch: GameMessagesInterface) => new FireCampAttackView(ViewModelDeckEvent(ch)))
  private val fireCampHealth: GameEvent  = (fireCampEvent_Health, (ch: GameMessagesInterface) => new FireCampHealthView(ViewModelDeckEvent(ch)))
  private val mycologists: GameEvent     = (mushRoomsExpertEvent, (ch: GameMessagesInterface) => new MycologistsView(ViewModelDeckEvent(ch)))
  private val sacrifice: GameEvent       = (sacrificeEvent, (ch: GameMessagesInterface) => new StrangeStonesView(ViewModelDeckEvent(ch)))
  private val getANewItem: GameEvent     = (getANewItemEvent, (ch: GameMessagesInterface) => new ItemSelectionView(ViewModelItemEvent(ch)))

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState

      val listEvents: List[GameEvent] = List(
        getANewItem,
        getANewItem,
        getANewItem,
        getANewItem,
        getANewItem,
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
        val ch = GameMessagesChannel()
        onViewChange(createView(ch))
        val nextState = event(gameState, ch)
        gameLoop(nextState, remainingEvents)