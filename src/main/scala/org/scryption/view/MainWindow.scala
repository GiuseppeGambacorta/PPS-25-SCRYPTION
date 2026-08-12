package org.scryption.view

import org.scryption.game.model.events.GameState
import org.scryption.{GUIChannel, GUIChannelInterface, GUIMessages}
import org.scryption.game.model.{Card, CardLibrary, Deck}
import org.scryption.game.model.events.*
import org.scryption.view.GUIGeometry.StartScreenGeometry
import org.scryption.view.events.{CardSelectionView, FireCampAttackView, FireCampHealthView, MycologistsView, StrangeStonesView}

import java.awt.Dimension
import scala.swing.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MainWindows extends SimpleSwingApplication:

  type GameEvent = (Event, GUIChannelInterface => FlowPanel)

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Main Window"

    // FIX 1: Set explicit size immediately
    size = new Dimension(1920, 1080)
    minimumSize = new Dimension(800, 600)

    // FIX 2: Simple BoxPanel with preferredSize set
    val centerContainer = new BoxPanel(Orientation.Vertical) {
      preferredSize = new Dimension(1920, 1080)
    }

    contents = centerContainer

    // --- Start Screen ---
    var gameRunning = false

    val startScreen = new StartScreenView(
      StartScreenGeometry(preferredSize.width),
      onNewGame = () => startNewGame(),
      onQuit = () => System.exit(0)
    )

    // Ensure start screen matches container
    startScreen.preferredSize = new Dimension(1920, 1080)
    centerContainer.contents += startScreen

    def startNewGame(): Unit =
      if (!gameRunning) {
        gameRunning = true
        val gameState = GameState(deck = Deck.getStandardDeck, isGameOver = false)

        val listEvents: List[GameEvent] = List(
          (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch)),
          (fireCamp_Attack, (ch: GUIChannelInterface) => new FireCampAttackView(channel = ch)),
          (fireCamp_Health, (ch: GUIChannelInterface) => new FireCampHealthView(channel = ch)),
          (mushRoomsExpert, (ch: GUIChannelInterface) => new MycologistsView(channel = ch)),
          (sacrifice, (ch: GUIChannelInterface) => new StrangeStonesView(channel = ch))
        )

        Future {
          try
            gameLoop(gameState, listEvents)
          finally
            Swing.onEDT {
              gameRunning = false
              cambiaVista(startScreen)
            }
        }
      }

    def cambiaVista(nuovaVista: Panel): Unit =
      Swing.onEDT {
        centerContainer.contents.clear()
        centerContainer.contents += nuovaVista

        // FIX 3: Force new view to match expected size
        nuovaVista.preferredSize = new Dimension(1920, 1080)

        centerContainer.revalidate()
        centerContainer.repaint()
      }

    def gameLoop(gameState: GameState, events: List[GameEvent]): Unit =
      (gameState, events) match
        case (_, Nil) =>
          println(s"=== FINE EVENTI ===")
          println(s"Mazzo Finale: ${gameState.deck}")
        case (GameState(_, endGame), _) if endGame =>
          println(s"=== GAME OVER ===")
          println(s"Mazzo Finale: ${gameState.deck}")
        case (_, (event, createView) :: remainingEvents) =>
          val ch = GUIChannel.getNewChannel
          cambiaVista(createView(ch))
          println("avvio evento")
          val nextState = event(gameState, ch)
          gameLoop(nextState, remainingEvents)