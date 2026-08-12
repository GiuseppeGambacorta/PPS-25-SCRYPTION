package org.scryption.game.model

import scala.annotation.targetName

type Slot = Option[Card[?]]
type BoardPosition = (Int, Int)

object boardModel:

  opaque type BoardRow = Vector[Slot]
  opaque type Board = Vector[BoardRow]

  val RowsCount: Int = 3
  val ColsCount: Int = 4

  val x: Slot = None
  val IndexOfBotRow = 1
  val IndexOfPlayerRow = 2
 
  extension (slot: Slot)
    infix def |(next: Slot): BoardRow = Vector(slot, next)

  extension (position: BoardPosition)
    def isValid : Boolean = (position._1 < RowsCount && position._2 < ColsCount) && (position._1 >= 0 && position._2 >= 0)


  extension (row: BoardRow)
    def apply(col: Int): Slot = row(col)
    def updated(col: Int, card: Slot): BoardRow = row.updated(col, card)

    @targetName("rowNumberOfCards")
    def numberOfCards: Int = row.count(_.isDefined)

    infix def |(next: Slot): BoardRow =
      val updatedRow = row :+ next
      require(updatedRow.length <= ColsCount, s"A row cannot exceed $ColsCount slots")
      updatedRow

    @targetName("rowConcat")
    infix def ||(nextRow: BoardRow): Board =
      require(row.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      require(nextRow.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      Vector(row, nextRow)


  extension (b: Board)
    def apply(row: Int): BoardRow = b(row)

    def updatedSlot(position: BoardPosition, card: Slot): Board = 
      val (row, col) = position
      b.updated(row, b(row).updated(col, card))
    
    
    def updateRow(row: Int, updatedRow: BoardRow): Board =
      b.updated(row, updatedRow)

    @targetName("boardNumberOfCards")
    def numberOfCards: Int = b.map(_.numberOfCards).sum

    @targetName("boardConcatRow")
    infix def ||(nextRow: BoardRow): Board =
      require(nextRow.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      val updated = b :+ nextRow
      require(updated.length <= RowsCount, s"A board cannot exceed $RowsCount rows")
      updated
  
  
  object BoardRow:
    def empty: BoardRow = Vector.fill(ColsCount)(None)

    def apply(slots: Slot*): BoardRow =
      require(slots.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      slots.toVector

  object Board:
    def empty: Board = Vector.fill(RowsCount)(BoardRow.empty)

    def apply(r0: BoardRow, r1: BoardRow, r2: BoardRow): Board = Vector(r0, r1, r2)