package org.scryption.game.model

import org.scryption.game.model.boardModel.Board

object SacrificeManager:

  /** Manages the sacrifices on the board by clearing the appropriate slots,
   * taking into account the ManyLives seal.
   *
   * @param board The current state of the board.
   * @param sacrificedSlots A list of (row, col) coordinates representing the cards for sacrifice.
   * @return the updated board after sacrifices are applied.*/
  def resolveSacrifices(board: Board, sacrificedSlots: List[(Int, Int)]): Board =
    sacrificedSlots.foldLeft(board): (currentBoard, coordinates) =>
      val (row, col) = coordinates
      currentBoard(row)(col) match
        case Some(card) if card.seals.contains(Seal.ManyLives) =>
          currentBoard
        case Some(_) =>
          currentBoard.updatedSlot(row, col, boardModel.x)
        case None => currentBoard
