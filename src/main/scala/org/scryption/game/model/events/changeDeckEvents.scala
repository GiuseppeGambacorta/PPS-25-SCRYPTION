package org.scryption.game.model.events

import java.util.concurrent.LinkedBlockingQueue
import org.scryption.game.model.*
import org.scryption.game.model.Deck
import org.scryption.game.model.events.GUIChannel.GUIChannel






import GUIChannel.*


case class GameState(deck: Deck.Deck, isGameOver: Boolean)
type Event = (GameState, GUIChannel) => GameState


enum GUIMessages:
  case Cards(cards: List[Card])
  case SingleCard(card: Card)
  case End



def GetANewCard(gameState: GameState, ch: GUIChannel): GameState = {

  ch.sendToGui(GUIMessages.Cards(List(CardLibrary.squirrel, CardLibrary.bear, CardLibrary.squirrel)))
  val message = ch.receiveFromGui

  message match {
    case GUIMessages.SingleCard(card) =>
      ch.sendToGui(GUIMessages.End)
      GameState(gameState.deck.addCard(card), gameState.isGameOver)
    case _ =>
      GetANewCard(gameState, ch)
  }
}


def SubstituteACard(gameState: GameState, ch: GUIChannel, f: (Card) => Card): GameState = {

  ch.sendToGui(GUIMessages.Cards(List(CardLibrary.squirrel, CardLibrary.bear, CardLibrary.squirrel)))
  val message = ch.receiveFromGui

  message match {
    case GUIMessages.SingleCard(card) =>
      ch.sendToGui(GUIMessages.End)
      gameState.deck.removeCard(card)
      GameState(gameState.deck.addCard(f(card)), gameState.isGameOver)
    case _ =>
      SubstituteACard(gameState, ch, f)
  }
}


def MushRooms(gameState: GameState, ch: GUIChannel): GameState =
  SubstituteACard(gameState, ch, Card => Card WithAttack Card.attack * 2 WithLife Card.life * 2)


def FireCamp_Attack(gameState: GameState, ch: GUIChannel): GameState =
  SubstituteACard(gameState, ch, Card => Card WithAttack Card.attack + 1) 

def FireCamp_Life(gameState: GameState, ch: GUIChannel): GameState =
  SubstituteACard(gameState, ch, Card => Card WithLife Card.life + 2)    
    
    



object GUIChannel:

  opaque type GUIChannel = (LinkedBlockingQueue[GUIMessages], LinkedBlockingQueue[GUIMessages])

  def getNewChannel: GUIChannel =
    (new LinkedBlockingQueue[GUIMessages](), new LinkedBlockingQueue[GUIMessages]())

  extension (ch: GUIChannel)
    // --- Metodi usati dal Thread del Gioco ---
    def sendToGui(message: GUIMessages): Unit = ch._1.put(message)
    def receiveFromGui: GUIMessages = ch._2.take() // Attende bloccando senza eccezioni

    // --- Metodi usati dal Thread della GUI ---
    def receiveFromGame: GUIMessages = ch._1.take() // Attende bloccando senza eccezioni
    def sendToGame(message: GUIMessages): Unit = ch._2.put(message)



// --- GAME LOOP ---
//def gameLoop(gameState: GameState, events: List[Event], view: Gui): Unit = {
//  (gameState, events) match {
//    case (_, Nil) =>
//      println(s"=== FINE EVENTI ===")
//      println(s"Mazzo Finale: ${gameState.deck}")
//
//   case (GameState(_, endGame), _) if endGame =>
//      println(s"=== GAME OVER ===")
//      println(s"Mazzo Finale: ${gameState.deck}")
//
//    case (_, currentEvent :: remainingEvents) =>
//      val ch = GUIChannel.getNewChannel
//      view.setChannel(ch)
//      val nextState = currentEvent(gameState, ch)
//      gameLoop(nextState, remainingEvents, view)
//  }
//}
