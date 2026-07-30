package org.scryption.game.model


import org.scryption.game.model.events.*
import org.scryption.game.model.events.GUIChannel.GUIChannel


trait Gui:
  def setChannel(channel: GUIChannel): Unit
  


def gameLoop(gameState: GameState, events: List[Event], view: Gui): Unit = {
  (gameState, events) match {
    case (_, Nil) =>
      println(s"=== FINE EVENTI ===")
      println(s"Mazzo Finale: ${gameState.deck}")

   case (GameState(_, endGame), _) if endGame =>
      println(s"=== GAME OVER ===")
      println(s"Mazzo Finale: ${gameState.deck}")

    case (_, currentEvent :: remainingEvents) =>
      val ch = GUIChannel.getNewChannel
      view.setChannel(ch)
      val nextState = currentEvent(gameState, ch)
      gameLoop(nextState, remainingEvents, view)
  }

}