package org.scryption

import org.scryption.GameEvents.GameEvent
import org.scryption.game.model.events.*
import org.scryption.game.model.{GameState, MapScript, MapTemplates}
import org.scryption.view.{ViewModelDeckEvent, ViewModelFight, ViewModelItemEvent, ViewModelMap}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.view.MapView
import org.scryption.GameMessagesChannel
import org.scryption.game.model.Maps.Path
import org.scryption.game.model.managers.SaveManager

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel
import scala.util.Random

object GameEvents:
  type GameEvent = (Event, GameMessagesChannel => Panel)

  val getANewCard: GameEvent = (getANewCardEvent, (ch: GameMessagesChannel) => new CardSelectionView(ViewModelDeckEvent(ch)))
  val fight: GameEvent = (fightEvent, (ch: GameMessagesChannel) => new FightView(ViewModelFight(ch)))
  val fireCampAttack: GameEvent = (fireCampEvent_Attack, (ch: GameMessagesChannel) => new FireCampAttackView(ViewModelDeckEvent(ch)))
  val fireCampHealth: GameEvent = (fireCampEvent_Health, (ch: GameMessagesChannel) => new FireCampHealthView(ViewModelDeckEvent(ch)))
  val mycologists: GameEvent = (mushRoomsExpertEvent, (ch: GameMessagesChannel) => new MycologistsView(ViewModelDeckEvent(ch)))
  val sacrifice: GameEvent = (sacrificeEvent, (ch: GameMessagesChannel) => new StrangeStonesView(ViewModelDeckEvent(ch)))
  val getANewItem: GameEvent = (getANewItemEvent, (ch: GameMessagesChannel) => new ItemSelectionView(ViewModelItemEvent(ch)))

  val listOfNotFightEvents = List(getANewItem, getANewCard, fireCampAttack, fireCampHealth, mycologists, sacrifice)
  def randomEvent : GameEvent = Random.shuffle(listOfNotFightEvents).head

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  import org.scryption.game.model.events.MapEvent

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState
      val initialScript = MapTemplates.newGameMap
      val mapPath = Path.fromScript(initialScript)

      Future {
        try
          gameLoop(initialState, mapPath, initialScript)
        finally
          running = false
          onGameOver()
      }

  def loadGame(): Unit =
    if !running then
      running = true
      Future {
        try
          SaveManager.loadGame() match
            case Some((loadedState, loadedScript)) =>
              val mapPath = Path.fromScript(loadedScript)
              println("Game loaded!")
              gameLoop(loadedState, mapPath, loadedScript, resumeFromMap = true)
            case None =>
              println("No save, start a new game...")
              val initialState = GameState.getInitialGameState
              val initialScript = MapTemplates.newGameMap
              val mapPath = Path.fromScript(initialScript)
              gameLoop(initialState, mapPath, initialScript)
        finally
          running = false
          onGameOver()
      }

  @tailrec
  private def gameLoop(gameState: GameState, map: Path[GameEvent], script: MapScript[GameEvent], resumeFromMap: Boolean = false): Unit =
    if gameState.isGameOver then
      println("=== GAME OVER ===")
    else
      val currentScript = Path.scrollScript(map, script)

      val (eventLogic , createView) = map match
        case Path.Node(event, _) => event match
          case (logic, view) => (logic, view)
        case _ => return
      val nextState = if resumeFromMap then
        gameState
      else
        val eventCh = GameMessagesChannel()
        onViewChange(createView(eventCh))
        eventLogic(gameState, eventCh)

      val hasNext = map match
        case Path.Node(_, Path.End()) => false
        case Path.End() => false
        case _ => true
      if !hasNext || nextState.isGameOver then
        println("=== GAME COMPLETED ===")
      else
        val mapCh = GameMessagesChannel()
        val vm = ViewModelMap(mapCh, map)
        onViewChange(new MapView(vm, () => {
          SaveManager.saveGame(nextState, currentScript)
        }))
        val nextMapPath = MapEvent(map, mapCh)
        gameLoop(nextState, nextMapPath, script, resumeFromMap = false)
