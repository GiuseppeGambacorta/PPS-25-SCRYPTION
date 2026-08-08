package org.scryption.game.model

import org.scryption.game.model.boardModel.*

object MovementManager:

  /** Manages the end of turn movements for a given row.
   *
   * @param row The current state of the board row.
   * @return The updated row after all movements are resolved.*/
  def resolveRowMovements(row: BoardRow): BoardRow =
    (0 until ColsCount).reverse.foldLeft(row): (currentRow, colIndex) =>
      currentRow(colIndex) match
        case Some(card) if card.seals.contains(Seal.Sprinter(Direction.Right)) =>
          val rightFree = colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty
          val leftFree = colIndex - 1 >= 0 && currentRow(colIndex - 1).isEmpty
          if rightFree then currentRow.updated(colIndex, boardModel.x).updated(colIndex + 1, Some(card))
          else if leftFree then
            val flippedCard = card removeSeal Seal.Sprinter(Direction.Right) addSeal Seal.Sprinter(Direction.Left)
            currentRow.updated(colIndex, boardModel.x).updated(colIndex - 1, Some(flippedCard))
          else
            currentRow
        case _ => currentRow
