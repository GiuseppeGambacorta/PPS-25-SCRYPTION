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

object RandomBotStrategy extends BotStrategy:
  override def playTurn(fightState: FightState): FightState =
    val random = new Random()
    var currentBoard = fightState.board
    val possibleCards = CardLibrary.getADeckWithAllTheLibrary.toList
    for col <- 0 until ColsCount do
      if random.nextBoolean() then
        val randomCard = possibleCards(random.nextInt(possibleCards.length))
        currentBoard = currentBoard.updatedSlot((IndexOfBotPrepRow, col), Some(randomCard))
    fightState.copy(board = currentBoard)
