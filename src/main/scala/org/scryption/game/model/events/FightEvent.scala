package org.scryption.game.model.events

import org.scryption.{GUIChannelInterface, GUIMessages}
import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.{CardLibrary, DrawDecks, PlayerHand}
import org.scryption.game.model.PlayerHand.PlayerHand
import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.events.TurnState.{botFight, botTurn, draw, playerTurn}

import scala.annotation.tailrec

enum TurnState:
  case draw
  case playerTurn
  case playerFight
  case botTurn
  case botFight
  
case class FightState(scale: Int, bones: Int, deck: Deck, hand: PlayerHand, board: Board)

def fight(gameState: GameState, ch: GUIChannelInterface): GameState =

  @tailrec
  def  loop(turnState: TurnState, fightState: FightState, ch: GUIChannelInterface): GameState =
    ch.sendToGui(GUIMessages.FightState(fightState))
    turnState match
      case TurnState.draw =>
        if fightState.scale <= -6 then GameState(gameState.deck, true)
        else
          ch.receiveFromGui match
            case GUIMessages.DrawFromSquirrel =>
              loop(playerTurn, fightState.copy(hand = fightState.hand addCard CardLibrary.squirrel), ch)
            case GUIMessages.DrawFromDeck =>
              val result = DrawDecks(fightState.deck).drawFromMain().get
              loop(playerTurn, fightState.copy(deck = result._2.mainDeck, hand = fightState.hand addCard result._1), ch)
      case TurnState.playerTurn => ???
        /* logic relative to the player turn */
      case TurnState.playerFight => ???
        /* after placing on the board, begin the fight checks */
      case TurnState.botTurn =>
        if fightState.scale >= 6 then gameState
        else
          loop(botFight, fightState, ch)
      case TurnState.botFight => ???
        /* same as playerFight */
          


  loop(draw,
       fightState = FightState(0, 0, gameState.deck, PlayerHand.fromList(gameState.deck.drawRandom(4)._1), Board.empty),
       ch)
  