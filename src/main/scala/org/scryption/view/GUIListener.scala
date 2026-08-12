package org.scryption

import org.scryption.game.model.Card
import org.scryption.view.events.CardSelectionView
import org.scryption.view.toViewInfo

import scala.swing.Swing

class GuiListener(channel: GUIChannelInterface) extends Thread {
  setDaemon(true)

  private var currentView: Option[CardSelectionView] = None
  private var currentCards: List[Card[?]] = Nil

  override def run(): Unit = {
    var running = true
    while (running) {
      channel.receiveFromGame match {
        case GUIMessages.Cards(cards) =>
          currentCards = cards

          Swing.onEDT {
            //currentView.foreach(v => v.close())

            val view = new CardSelectionView(
              channel = channel
            )

            currentView = Some(view)

            //val viewInfos = cards.map(_.toViewInfo)
            //view.showCards(viewInfos)
          }

        case GUIMessages.SingleCard(card) =>
        // println(s"Listener received echo: ${card.name}")

        case GUIMessages.End =>
          running = true
         // Swing.onEDT {
          //  currentView.foreach(_.close())
         // }
      }
    }
  }
}