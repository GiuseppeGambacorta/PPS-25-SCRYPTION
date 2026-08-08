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
          if colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty then
            currentRow.updated(colIndex, boardModel.x).updated(colIndex + 1, Some(card))
          else
            currentRow
        case _ => currentRow
