package org.scryption.game.model.managers

import org.scryption.game.model.boardModel.*
import org.scryption.game.model.{Card, Direction, Seal, boardModel}

import java.util.UUID

/** A trait representing a manager for card movements on the board.
  */
trait MovementManager:

  /** Manages the end of turn movements for a given row. Resolves the effects of movement seals (like Sprinter)
    * sequentially.
    *
    * @param row
    *   The current state of the board row.
    * @return
    *   The updated row after all movements are resolved.
    */
  def resolveRowMovements(row: BoardRow): BoardRow

  /** Manages the Guardian seal reaction when an opponent plays a card. Moves a card with the Guardian seal to the slot
    * opposing the newly played card, if possible.
    *
    * @param row
    *   The current state of the player's board row.
    * @param playedCol
    *   The column index where the opponent just played a card.
    * @return
    *   The updated row if a Guardian moved, or the original row otherwise.
    */
  def resolveGuardianMovement(row: BoardRow, playedCol: Int): BoardRow

  /** Moves cards from the bot's preparation row (row 0) to the attack row (row 1) if the attack slot in front of them
    * is empty.
    *
    * @param board
    *   The current state of the board.
    * @return
    *   The updated board.
    */
  def resolveBotQueueMovement(board: Board): Board

object MovementManager:

  /** Creates a [[MovementManager]] with the default implementation.
    *
    * @return
    *   the default movement manager.
    */
  def apply(): MovementManager = new MovementManagerImpl()

  private class MovementManagerImpl extends MovementManager:

    private def getNewRowMoveRight(colIndex: Int, currentRow: BoardRow, card: Card[?]): BoardRow =
      currentRow.updated(colIndex, boardModel.x).updated(colIndex + 1, Some(card))

    private def getNewRowMoveLeft(colIndex: Int, currentRow: BoardRow, card: Card[?]): BoardRow =
      currentRow.updated(colIndex, boardModel.x).updated(colIndex - 1, Some(card))

    override def resolveRowMovements(row: BoardRow): BoardRow =
      val initiateState: (BoardRow, Set[UUID]) = (row, Set.empty[UUID])
      val (finalRow, _) = (0 until ColsCount).foldLeft(initiateState): (acc, colIndex) =>
        val (currentRow, movedCards) = acc
        currentRow(colIndex) match
          case Some(card) if !movedCards.contains(card.id) =>
            card.seals match
              case seals if seals.contains(Seal.Sprinter(Direction.Right)) =>
                val rightFree = colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty
                val leftFree = colIndex - 1 >= 0 && currentRow(colIndex - 1).isEmpty
                if rightFree then
                  val newRow = getNewRowMoveRight(colIndex, currentRow, card)
                  (newRow, movedCards + card.id)
                else if leftFree then
                  val flippedCard =
                    card.removeSeal(Seal.Sprinter(Direction.Right)).addSeal(Seal.Sprinter(Direction.Left))
                  val newRow = getNewRowMoveLeft(colIndex, currentRow, flippedCard)
                  (newRow, movedCards + card.id)
                else acc
              case seals if seals.contains(Seal.Sprinter(Direction.Left)) =>
                val leftFree = colIndex - 1 >= 0 && currentRow(colIndex - 1).isEmpty
                val rightFree = colIndex + 1 < ColsCount && currentRow(colIndex + 1).isEmpty
                if leftFree then
                  val newRow = getNewRowMoveLeft(colIndex, currentRow, card)
                  (newRow, movedCards + card.id)
                else if rightFree then
                  val flippedCard =
                    card.removeSeal(Seal.Sprinter(Direction.Left)).addSeal(Seal.Sprinter(Direction.Right))
                  val newRow = getNewRowMoveRight(colIndex, currentRow, flippedCard)
                  (newRow, movedCards + card.id)
                else acc
              case _ => acc
          case _ => acc
      finalRow

    override def resolveGuardianMovement(row: BoardRow, playedCol: Int): BoardRow =
      if playedCol < 0 || playedCol >= boardModel.ColsCount || row(playedCol).isDefined then row
      else
        val guardianIndexOpt = (0 until boardModel.ColsCount).find: colIndex =>
          row(colIndex) match
            case Some(card) => card.seals.contains(Seal.Guardian)
            case None       => false
        guardianIndexOpt match
          case Some(originalIndex) =>
            row(originalIndex) match
              case Some(card) => row.updated(originalIndex, boardModel.x).updated(playedCol, Some(card))
              case None       => row
          case None => row

    override def resolveBotQueueMovement(board: Board): Board =
      val IndexOfBotPrepRow = 0
      (0 until boardModel.ColsCount).foldLeft(board): (currentBoard, col) =>
        if currentBoard(IndexOfBotRow)(col).isEmpty && currentBoard(IndexOfBotPrepRow)(col).isDefined then
          val cardToMove = currentBoard(IndexOfBotPrepRow)(col)
          currentBoard
            .updatedSlot((IndexOfBotRow, col), cardToMove)
            .updatedSlot((IndexOfBotPrepRow, col), None)
        else currentBoard
