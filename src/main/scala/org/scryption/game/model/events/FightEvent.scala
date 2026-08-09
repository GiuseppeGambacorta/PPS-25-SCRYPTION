package org.scryption.game.model.events

import org.scryption.{GUIChannelInterface, GUIMessages}
import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.{CardLibrary, DrawDecks, PlayerHand, SacrificeAttribute}
import org.scryption.game.model.PlayerHand.PlayerHand
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.SacrificeAttribute.{Blood, Bones}
import org.scryption.game.model.managers.SacrificeManager

import scala.annotation.tailrec

enum TurnState:
  case draw
  case playerTurn
  case playerFight
  case botTurn
  case botFight

case class FightState(scalePoints: Int, bones: Int, deck: Deck, playerHand: PlayerHand, board: Board)

val PlayerWinningPoints = 6
val BotWinningPoints = -6
val NumberOfCardsAtTheStartOfTheFight = 4
val PlayerWon = true
val PLayerLose = false

def fight(gameState: GameState, ch: GUIChannelInterface): GameState =
  val (initialCards, remainingDeck) = gameState.deck.drawRandom(NumberOfCardsAtTheStartOfTheFight)
  val initialFightState = FightState(
    scalePoints = 0,
    bones = 0,
    deck = remainingDeck,
    playerHand = PlayerHand.fromList(initialCards),
    board = Board.empty
  )

  GameState(gameState.deck, isGameOver = loop(TurnState.draw, initialFightState,  ch))

@tailrec
private def loop(turnState: TurnState, fightState: FightState, ch: GUIChannelInterface): Boolean =

  ch.sendToGui(GUIMessages.FightState(fightState))

  turnState match
    case TurnState.draw =>
      if fightState.scalePoints <= BotWinningPoints then PLayerLose
      else
        ch.receiveFromGui match
          case GUIMessages.DrawFromSquirrel =>
            val updatedHand = fightState.playerHand addCard CardLibrary.squirrel
            loop(TurnState.playerTurn, fightState.copy(playerHand = updatedHand),ch)

          case GUIMessages.DrawFromDeck =>
            DrawDecks(fightState.deck).drawFromMain() match
              case Some((drawnCard, updatedDrawDecks)) =>
                val nextState = fightState.copy(deck = updatedDrawDecks.mainDeck, playerHand = fightState.playerHand addCard drawnCard)
                loop(TurnState.playerTurn, nextState,  ch)
              case None =>
                loop(TurnState.draw, fightState, ch)

          case _ =>
            ch.clear()
            loop(TurnState.draw, fightState,  ch)

    case TurnState.playerTurn =>
      ch.receiveFromGui match
        case GUIMessages.CardToPlay(card, position) =>

          fightState.board(IndexOfPlayerRow)(position) match
            case None =>

              card.sacrificeAttribute match
                case SacrificeAttribute.Nil => loop(TurnState.playerTurn, fightState.copy(board = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))), ch)
                case SacrificeAttribute.Bones(amount) if amount >= fightState.bones =>
                  val newBoard = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))
                  val newBonesAmount = fightState.bones - amount
                  loop(TurnState.playerTurn, fightState.copy(board = newBoard, bones = newBonesAmount), ch)
                case _ => loop(TurnState.playerTurn, fightState, ch)

            case Some(_) => loop(TurnState.playerTurn, fightState, ch)


        case GUIMessages.CardToPlayWithSacrifices(card, position, sacrificesPositions) =>
          card.sacrificeAttribute match
            case Blood(amount) =>
  
              fightState.board(IndexOfPlayerRow)(position) match
                case None =>
                  val sacrificeResult = SacrificeManager.resolveSacrifices(fightState.board, sacrificesPositions)
                  if sacrificeResult.generatedBlood >= amount then
                    val newBoard = sacrificeResult.updatedBoard.updatedSlot((IndexOfPlayerRow, position), Some(card))
                    val newBonesAmount = fightState.bones + sacrificeResult.generatedBones
                    loop(TurnState.playerTurn, fightState.copy(board = newBoard, bones = newBonesAmount), ch)
                  else
                    loop(TurnState.playerTurn, fightState, ch)
    
                case Some(_) if sacrificesPositions.contains((IndexOfPlayerRow, position)) =>
                  val sacrificeResult = SacrificeManager.resolveSacrifices(fightState.board, sacrificesPositions)
                  if sacrificeResult.generatedBlood >= amount then
                    val newBoard = sacrificeResult.updatedBoard.updatedSlot((IndexOfPlayerRow, position), Some(card))
                    val newBonesAmount = fightState.bones + sacrificeResult.generatedBones
                    loop(TurnState.playerTurn, fightState.copy(board = newBoard, bones = newBonesAmount), ch)
                  else 
                    loop(TurnState.playerTurn, fightState, ch)
        
        case _ => loop(TurnState.playerTurn, fightState, ch)

      
   

    case TurnState.playerFight =>
      // TODO: Calcolare i danni inflitti dal giocatore, aggiornare scalePoints/bones
      if fightState.scalePoints >= PlayerWinningPoints then PlayerWon
      else
        loop(TurnState.botTurn, fightState, ch)

    case TurnState.botTurn =>
      if fightState.scalePoints >= PlayerWinningPoints then PlayerWon
      else
        // TODO: IA / Bot sceglie ed esegue le sue mosse
        loop(TurnState.botFight, fightState, ch)

    case TurnState.botFight =>
      // TODO: Calcolare i danni inflitti dal bot, aggiornare scalePoints
      if fightState.scalePoints <= BotWinningPoints then PLayerLose
      else
        loop(TurnState.draw, fightState, ch)
