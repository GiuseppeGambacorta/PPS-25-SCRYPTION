package org.scryption.game.model.managers

import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.{BoardPosition, Seal, boardModel}

/** Represents the result of a sacrifice
 */
case class SacrificeResult(updatedBoard: Board, generatedBlood: Int, generatedBones: Int)

object SacrificeManager:

  /** Manages the sacrifices on the board by clearing the appropriate slots,
   * taking into account the ManyLives seal, calculating the generated blood and bones.
   *
   * @param board The current state of the board.
   * @param sacrificedSlots A list of (row, col) coordinates representing the cards for sacrifice.
   * @return the updated board after sacrifices are applied.
   */
  def resolveSacrifices(board: Board, sacrificedSlots: List[BoardPosition]): SacrificeResult =
    val initialState = SacrificeResult(board, 0, 0)
    sacrificedSlots.foldLeft(initialState): (acc, coordinates) =>
      val (row, col) = coordinates
      val currentBoard = acc.updatedBoard
      val currentBlood = acc.generatedBlood
      val currentBones = acc.generatedBones
      currentBoard(row)(col) match
        case Some(card) if card.seals.contains(Seal.ManyLives) =>
          SacrificeResult(currentBoard, currentBlood + 1, currentBones)
        case Some(card) =>
          val newBoard = currentBoard.updatedSlot((row,col), boardModel.x)
          val bonesToGain = if card.seals.contains(Seal.BoneKing) then 4 else 1
          SacrificeResult(newBoard, currentBlood + 1, currentBones + bonesToGain)
        case None => acc
