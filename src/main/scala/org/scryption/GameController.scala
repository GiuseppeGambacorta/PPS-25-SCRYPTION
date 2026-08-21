package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.GameState
import org.scryption.view.{ViewModelDeckEvent, ViewModelFight, ViewModelItemEvent, ViewModelMap}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.view.MapView
import org.scryption.GameMessagesChannel
import org.scryption.game.model.managers.SaveManager

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel

object GameEvents:
  type GameEvent = (Event, GameMessagesChannel => Panel)

  val getANewCard: GameEvent = (getANewCardEvent, (ch: GameMessagesChannel) => new CardSelectionView(ViewModelDeckEvent(ch)))
  val fight: GameEvent = (fightEvent, (ch: GameMessagesChannel) => new FightView(ViewModelFight(ch)))
  val fireCampAttack: GameEvent = (fireCampEvent_Attack, (ch: GameMessagesChannel) => new FireCampAttackView(ViewModelDeckEvent(ch)))
  val fireCampHealth: GameEvent = (fireCampEvent_Health, (ch: GameMessagesChannel) => new FireCampHealthView(ViewModelDeckEvent(ch)))
  val mycologists: GameEvent = (mushRoomsExpertEvent, (ch: GameMessagesChannel) => new MycologistsView(ViewModelDeckEvent(ch)))
  val sacrifice: GameEvent = (sacrificeEvent, (ch: GameMessagesChannel) => new StrangeStonesView(ViewModelDeckEvent(ch)))
  val getANewItem: GameEvent = (getANewItemEvent, (ch: GameMessagesChannel) => new ItemSelectionView(ViewModelItemEvent(ch)))
  val trial     : GameEvent = (trialEvent, (ch: GameMessagesChannel) => new TrialView(ViewModelDeckEvent(ch)))
  
  val listOfNotFightEvents = List(getANewItem, getANewCard, fireCampAttack, fireCampHealth, mycologists, sacrifice)

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  import org.scryption.game.model.GameMap
  import org.scryption.game.model.events.MapEvent

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState
      val map = GameMap()
      startGameThread(initialState, map)

  def loadGame(): Unit =
    if !running then
      SaveManager.loadGame() match
        case Some((loadedState, loadedMap)) =>
          running = true
          Future {
            try
              resumeFromMap(loadedState, loadedMap)
            finally
              running = false
              onGameOver()
          }
        case None =>
          println("No save found")

  private def resumeFromMap(gameState: GameState, map: GameMap): Unit =
    val currentNode = map.Left
    val hasNext = currentNode.nextNode.isDefined || currentNode.left.isDefined || currentNode.right.isDefined

    if !hasNext || gameState.isGameOver then
      println("Game ended")
    else
      val mapCh = GameMessagesChannel()
      val vm = ViewModelMap(mapCh, map, gameState)
      onViewChange(new MapView(vm))
      val nextMap = MapEvent(map, mapCh)
      gameLoop(gameState, nextMap)

  private def startGameThread(state: GameState, map: GameMap): Unit =
    Future {
      try gameLoop(state, map)
      finally
        running = false
        onGameOver()
    }

  @tailrec
  private def gameLoop(gameState: GameState, map: GameMap): Unit =
    if gameState.isGameOver then
      println("=== GAME OVER ===")
    else

      val (eventLogic, createView) = map.currentEvent
      val eventCh = GameMessagesChannel()
      onViewChange(createView(eventCh))
      val nextState = eventLogic(gameState, eventCh)

      val currentNode = map.Left
      val hasNext = currentNode.nextNode.isDefined || currentNode.left.isDefined || currentNode.right.isDefined

      if !hasNext || nextState.isGameOver then
        println("=== PARTITA COMPLETATA ===")
      else

        val mapCh = GameMessagesChannel()
        val vm = ViewModelMap(mapCh, map, nextState)
        onViewChange(new MapView(vm))

        val nextMap = MapEvent(map, mapCh)

        gameLoop(nextState, nextMap)
