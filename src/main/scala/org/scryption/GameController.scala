package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.GameState
import org.scryption.view.{ViewModelDeckEvent, ViewModelFight, ViewModelItemEvent, ViewModelMap}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.view.MapView
import org.scryption.{GameMessagesChannel, GameMessagesInterface}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel



object GameEvents:
  type GameEvent = (Event, GameMessagesInterface => Panel)
  val getANewCard: GameEvent = (getANewCardEvent, (ch: GameMessagesInterface) => new CardSelectionView(ViewModelDeckEvent(ch)))
  val fight: GameEvent = (fightEvent, (ch: GameMessagesInterface) => new FightView(ViewModelFight(ch)))
  val fireCampAttack: GameEvent = (fireCampEvent_Attack, (ch: GameMessagesInterface) => new FireCampAttackView(ViewModelDeckEvent(ch)))
  val fireCampHealth: GameEvent = (fireCampEvent_Health, (ch: GameMessagesInterface) => new FireCampHealthView(ViewModelDeckEvent(ch)))
  val mycologists: GameEvent = (mushRoomsExpertEvent, (ch: GameMessagesInterface) => new MycologistsView(ViewModelDeckEvent(ch)))
  val sacrifice: GameEvent = (sacrificeEvent, (ch: GameMessagesInterface) => new StrangeStonesView(ViewModelDeckEvent(ch)))
  val getANewItem: GameEvent = (getANewItemEvent, (ch: GameMessagesInterface) => new ItemSelectionView(ViewModelItemEvent(ch)))

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):

  import GameEvents.*
  import org.scryption.game.model.events.{GameMap, MapEvent}

  @volatile private var running = false

  def startNewGame(): Unit =
    if !running then
      running = true
      val initialState = GameState.getInitialGameState

      /*
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
      */
      val map = GameMap()

      Future {
        try
          gameLoop(initialState, map)
        finally
          running = false
          onGameOver()
      }

  @tailrec

  private def gameLoop(gameState: GameState, map: GameMap): Unit =
    // 1. Condizione di sconfitta/fine forzata
    if gameState.isGameOver then
      println("=== PARTITA TERMINATA (Game Over) ===")
      println(s"Mazzo Finale: ${gameState.deck}")
    else
     
      val ch = GameMessagesChannel()
      onViewChange(map.ActualEvent._2(ch)) // o la view legata all'evento
      val nextState = map.ActualEvent._1(gameState, ch)

      
      val currentNode = map.Left
      val hasNextSteps = currentNode.nextNode.isDefined || currentNode.left.isDefined || currentNode.right.isDefined

      if !hasNextSteps || nextState.isGameOver then
        println("=== PARTITA TERMINATA ===")
        println(s"Mazzo Finale: ${nextState.deck}")
      else
       
        val mapCh = GameMessagesChannel()
        onViewChange(MapView(ViewModelMap(mapCh, map)))
        val nextMap = MapEvent(map, mapCh)

        // 5. Passo ricorsivo con nuovo stato e mappa avanzata
        gameLoop(nextState, nextMap)