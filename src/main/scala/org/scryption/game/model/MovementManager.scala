package org.scryption.game.model

import org.scryption.game.model.boardModel.*

import java.util.UUID

object MovementManager:

  private def getNewRowMoveRight(colIndex: Int, currentRow: BoardRow, card: Card[?]): BoardRow =
    currentRow.updated(colIndex, boardModel.x).updated(colIndex + 1, Some(card))

  private def getNewRowMoveLeft(colIndex: Int, currentRow: BoardRow, card: Card[?]): BoardRow =
    currentRow.updated(colIndex, boardModel.x).updated(colIndex - 1, Some(card))

  /** Manages the end of turn movements for a given row.
   *
   * @param row The current state of the board row.
   * @return The updated row after all movements are resolved.*/
  def resolveRowMovements(row: BoardRow): BoardRow =
    val initiateState: (BoardRow, Set[UUID]) = (row, Set.empty[UUID])
    val (finalRow, _) = (0 until ColsCount).foldLeft(initiateState): (acc, colIndex) =>
      val (currentRow, movedCards) = acc
      currentRow(colIndex) match
        case Some(card) if !movedCards.contains(card.id) =>
          if card.seals.contains(Seal.Sprinter(Direction.Right)) then
            val rightFree = colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty
            val leftFree = colIndex - 1 >= 0 && currentRow(colIndex - 1).isEmpty
            if rightFree then
              val newRow = getNewRowMoveRight(colIndex, currentRow, card)
              (newRow, movedCards + card.id)
            else if leftFree then
              val flippedCard = card removeSeal Seal.Sprinter(Direction.Right) addSeal Seal.Sprinter(Direction.Left)
              val newRow = getNewRowMoveLeft(colIndex, currentRow, flippedCard)
              (newRow, movedCards + card.id)
            else
              acc
          else if card.seals.contains(Seal.Sprinter(Direction.Left)) then
            val leftFree = colIndex - 1 >= 0 && currentRow(colIndex - 1).isEmpty
            val rightFree = colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty
            if leftFree then
              val newRow = getNewRowMoveLeft(colIndex, currentRow, card)
              (newRow, movedCards + card.id)
            else if rightFree then
              val flippedCard = card removeSeal Seal.Sprinter(Direction.Left) addSeal Seal.Sprinter(Direction.Right)
              val newRow = getNewRowMoveRight(colIndex, currentRow, flippedCard)
              (newRow, movedCards + card.id)
            else
              acc
          else
            acc
        case _ => acc
    finalRow
