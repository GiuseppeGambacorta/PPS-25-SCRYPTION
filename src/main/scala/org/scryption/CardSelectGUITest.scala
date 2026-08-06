package org.scryption

import org.scryption.game.model.{CardLibrary, Deck}
import org.scryption.game.model.events.{GameState, Event, getANewCard, fireCamp_Attack}

object CardSelectGUITest {

  def main(args: Array[String]): Unit = {

    val channel: GUIChannelInterface = GUIChannel.getNewChannel

    val listener = new GuiListener(channel)
    listener.start()

    var currentState = GameState(Deck.empty, isGameOver = false)

    val testEvents: List[Event] = List(getANewCard, getANewCard)
    

    var runningState = currentState
    var eventCount = 1

    testEvents.foreach { event =>

      try {
        runningState = event(runningState, channel)
        println(s"    Deck: ${runningState.deck.size}")

        Thread.sleep(1000)
        eventCount += 1
      } catch {
        case e: Exception =>
          println(s"[ERROR] In event ${eventCount}: ${e.getMessage}")
          e.printStackTrace()
      }
    }

    channel.sendToGui(GUIMessages.End)

    Thread.sleep(1000)
    listener.interrupt()
  }
}