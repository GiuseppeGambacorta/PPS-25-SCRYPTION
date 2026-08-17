package org.scryption.game.model.bot

import org.scryption.game.model.CardLibrary
import org.scryption.game.model.boardModel.{ColsCount, IndexOfBotPrepRow}
import org.scryption.game.model.events.FightState

import scala.util.Random

trait BotStrategy:
  /** Calculate and executes the bot plays.
   *
   * @param fightState The current state of the fight.
   * @return the updated state of the fight after the bot plays.
   */
  def playTurn(fightState: FightState): FightState

case class RandomBotStrategy(val maxCardsPerTurn: Int = 1) extends BotStrategy:

  override def playTurn(fightState: FightState): FightState =
    val random = new Random()
    val possibleCards = CardLibrary.getADeckWithAllTheLibrary.toList
    val emptyCols = (0 until ColsCount).filter: col =>
      fightState.board(IndexOfBotPrepRow)(col).isEmpty
    .toList
    val colsToFill = random.shuffle(emptyCols).take(maxCardsPerTurn)
    val finalBoard = colsToFill.foldLeft(fightState.board): (board, col) =>
      val randomCard = possibleCards(random.nextInt(possibleCards.length))
      board.updatedSlot((IndexOfBotPrepRow, col), Some(randomCard))
    fightState.copy(board = finalBoard)
