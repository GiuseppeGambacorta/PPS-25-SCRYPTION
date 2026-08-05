package org.scryption.view

import org.scryption.game.model.events.GameState
import org.scryption.{GUIChannel, GUIMessages, GUIChannelInterface}
import org.scryption.game.model.{Card, CardLibrary, Deck}
import org.scryption.game.model.events.*

import java.awt.Dimension
import scala.swing.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MainWindows extends SimpleSwingApplication:

  type GameEvent = (Event, GUIChannelInterface => CardSelectionView)

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Main Window"
    preferredSize = new Dimension(1280, 900)

    val centerContainer = new BoxPanel(Orientation.Vertical)
    contents = centerContainer

    // --- Pannello Iniziale ---
    val startButton = new Button("Start Game")
    val startPanel = new FlowPanel:
      contents += startButton

    // Imposta il pannello di start all'avvio dell'applicazione
    cambiaVista(startPanel)

    // Gestione del click sul pulsante Start
    startButton.action = Action("Start Game") {
      startButton.enabled = false // Disabilita per evitare doppi click

      val gameState = GameState(deck = Deck.getStandardDeck, isGameOver = false)

      val listEvents: List[GameEvent] = List(
        (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch)),
        (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch))
      )

    
      Future {
        try
          gameLoop(gameState, listEvents)
        finally
          Swing.onEDT {
            startButton.enabled = true
            cambiaVista(startPanel)
          }
      }
    }

    def cambiaVista(nuovaVista: Panel): Unit =
      Swing.onEDT {
        centerContainer.contents.clear()
        centerContainer.contents += nuovaVista
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