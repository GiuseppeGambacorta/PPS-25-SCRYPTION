package org.scryption

import org.scryption.GameEvents.GameEvent
import org.scryption.game.model.GameState
import org.scryption.game.model.MapScript
import org.scryption.game.model.MapTemplates
import org.scryption.game.model.Maps.Path
import org.scryption.game.model.events.*
import org.scryption.game.model.managers.SaveManager
import org.scryption.view.MapView
import org.scryption.view.ViewModelDeckEvent
import org.scryption.view.ViewModelFight
import org.scryption.view.ViewModelItemEvent
import org.scryption.view.ViewModelMap
import org.scryption.view.events.*
import org.scryption.view.fight.FightView

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel
import scala.util.Random

object GameEvents:
  type GameEvent = (Event, GameMessagesChannel => Panel)

  val getANewCard: GameEvent =
    (getANewCardEvent, (ch: GameMessagesChannel) => new CardSelectionView(ViewModelDeckEvent(ch)))
  val fight: GameEvent = (fightEvent, (ch: GameMessagesChannel) => new FightView(ViewModelFight(ch)))
  val fireCampAttack: GameEvent =
    (fireCampEvent_Attack, (ch: GameMessagesChannel) => new FireCampAttackView(ViewModelDeckEvent(ch)))
  val fireCampHealth: GameEvent =
    (fireCampEvent_Health, (ch: GameMessagesChannel) => new FireCampHealthView(ViewModelDeckEvent(ch)))
  val mycologists: GameEvent =
    (mushRoomsExpertEvent, (ch: GameMessagesChannel) => new MycologistsView(ViewModelDeckEvent(ch)))
  val sacrifice: GameEvent =
    (sacrificeEvent, (ch: GameMessagesChannel) => new StrangeStonesView(ViewModelDeckEvent(ch)))
  val getANewItem: GameEvent =
    (getANewItemEvent, (ch: GameMessagesChannel) => new ItemSelectionView(ViewModelItemEvent(ch)))

  val listOfNotFightEvents = List(getANewItem, getANewCard, fireCampAttack, fireCampHealth, mycologists, sacrifice)

  /** Picks a random non-fight event among the available ones. */
  def randomEvent: GameEvent = Random.shuffle(listOfNotFightEvents).head

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  import org.scryption.game.model.events.MapEvent

  @volatile private var running = false

  /** Starts a brand new game: builds the initial state and map, then runs the game loop
   * on a background `Future` so the EDT is never blocked while waiting on the channel.
   * Does nothing if a game is already running.
   */
  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState
      val initialScript = MapTemplates.newGameMap
      val mapPath = Path.fromScript(initialScript)

      Future {
        try gameLoop(initialState, mapPath)
        finally
          running = false
          onGameOver()
      }

  /** Loads a previously saved game and resumes the game loop from the saved map position,
   * falling back to a brand new game if no save is found. Runs on a background `Future`.
   * Does nothing if a game is already running.
   */
  def loadGame(): Unit =
    if !running then
      running = true
      Future {
        try
          SaveManager.loadGame() match
            case Some((loadedState, loadedPath)) =>
              println("Game loaded!")
              gameLoop(loadedState, loadedPath, resumeFromMap = true)
            case None =>
              println("No save, start a new game...")
              val initialState = GameState.getInitialGameState
              val initialScript = MapTemplates.newGameMap
              val mapPath = Path.fromScript(initialScript)
              gameLoop(initialState, mapPath)
        finally
          running = false
          onGameOver()
      }

  /** Tail-recursive core loop of the game. For each map node, instantiates a dedicated
   * channel, publishes the corresponding view, and runs the event's domain logic to
   * obtain the next state. If the game continues, mounts the map view, resolves the
   * player's path choice via `MapEvent`, and recurses onto the resulting map position.
   */
  @tailrec
  private def gameLoop(gameState: GameState, map: Path[GameEvent], resumeFromMap: Boolean = false): Unit =
    if gameState.isGameOver then println("=== GAME OVER ===")
    else
      val (eventLogic, createView) = map match
        case Path.Node(event, _) =>
          event match
            case (logic, view) => (logic, view)
        case _ => return

      val nextState =
        if resumeFromMap then gameState
        else
          val eventCh = GameMessagesChannel()
          onViewChange(createView(eventCh))
          eventLogic(gameState, eventCh)

      val hasNext = map match
        case Path.Node(_, Path.End()) => false
        case Path.End()               => false
        case _                        => true

      if !hasNext || nextState.isGameOver then println("=== GAME COMPLETED ===")
      else
        val mapCh = GameMessagesChannel()
        val vm = ViewModelMap(mapCh, map)

        onViewChange(
          new MapView(
            vm,
            () => {
              SaveManager.saveGame(nextState, map)
            }
          )
        )

        val nextMapPath = MapEvent(map, mapCh)
        gameLoop(nextState, nextMapPath, resumeFromMap = false)