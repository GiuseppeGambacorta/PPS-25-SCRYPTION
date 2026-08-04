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

    // Dimensioni adeguate al layout delle carte e dell'offset
    preferredSize = new Dimension(1280, 900)

    val centerContainer = new BoxPanel(Orientation.Vertical)
    contents = centerContainer

    val gameState = GameState(deck = Deck.getStandardDeck, isGameOver = false)

    val listEvents: List[GameEvent] = List(
      (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch)),
      (getANewCard, (ch: GUIChannelInterface) => new CardSelectionView(channel = ch))
    )

    // Avvia il loop di gioco in un thread separato per non bloccare l'EDT
    Future {
      gameLoop(gameState, listEvents)
    }

    def cambiaVista(nuovaVista: Panel): Unit =
      // Gli aggiornamenti grafici Swing vanno sempre eseguiti sull'EDT
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

          // 1. Mostra la nuova vista sulla UI
          cambiaVista(createView(ch))
          println("avvio gioco")
          // 2. Esegue l'evento (attende la risposta dal canale in background)
          val nextState = event(gameState, ch)

          // 3. Prosegue con il prossimo evento
          gameLoop(nextState, remainingEvents)