package org.scryption

import org.scryption.game.model.events.*
import org.scryption.game.model.GameState
import org.scryption.view.{MapView, ViewModelEvent, ViewModelFight, ViewModelMap}
import org.scryption.view.events.*
import org.scryption.view.fight.FightView
import org.scryption.{GUIChannel, GUIChannelInterface}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Panel


type GameEvent = (Event, GUIChannelInterface => Panel)

class GameController(onViewChange: Panel => Unit, onGameOver: () => Unit):



  //private val map : GameEvent = (MapEvent, (ch: GUIChannelInterface) => new MapView(ViewModelMap(ch)))

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
      val initialState = GameState()

      val listEvents: List[GameEvent] = List(
        getANewCard,
        fight,
        fireCampAttack,
        fireCampHealth,
        mycologists,
        sacrifice
      )


      val end : Path = getANewCard :: fight :: Path.Nil
      val bifork

      val begin : Path =  fight :: getANewCard :: Path.Nil




      Future {
        try
          gameLoop(initialState, path)
        finally
          running = false
          onGameOver()
      }

  @tailrec
  private def gameLoop(gameState: GameState, map : Path): Unit =
    (gameState, map) match
      case (_, Path.Nil) | (GameState(_, true), _) =>
        println(s"=== PARTITA TERMINATA ===")
        println(s"Mazzo Finale: ${gameState.deck}")
      case (_, Path.Node( (event,view), nextEvent)) =>




        val ch = GUIChannel()
        onViewChange(view(ch))
        val nextState = event(gameState, ch)


        onViewChange(new MapView(ViewModelMap(ch)))
        val newMap = MapEvent(nextEvent, ch)

        gameLoop(nextState, newMap)