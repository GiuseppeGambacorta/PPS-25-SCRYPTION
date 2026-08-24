package org.scryption.game.model

import scala.annotation.targetName

/** Represents the state of a single board cell: `Some(card)` if occupied by a card, `None` if empty. */
type Slot = Option[Card[?]]

/** (row, column) coordinates of a position on the board. */
type BoardPosition = (Int, Int)

/** Module that defines the representation of the game grid (`Board`) and its rows (`BoardRow`), together with the
  * declarative DSL used to build them via the `|` and `||` operators.
  */
object boardModel:

  /** A row of the board, an opaque type backed by `Vector[Slot]`. Encapsulation prevents the underlying structure from
    * being created or modified outside of the constructors and extension methods exposed by this module.
    */
  opaque type BoardRow = Vector[Slot]

  /** The full game grid, an opaque type backed by `Vector[BoardRow]`. As with `BoardRow`, the internal representation
    * is encapsulated and can only be manipulated through the controlled operations defined here.
    */
  opaque type Board = Vector[BoardRow]

  val RowsCount: Int = 3
  val ColsCount: Int = 4

  val x: Slot = None

  val IndexOfBotPrepRow = 0
  val IndexOfBotRow = 1
  val IndexOfPlayerRow = 2

  extension (slot: Slot)
    /** Starts a `BoardRow` by concatenating two consecutive `Slot`s. DSL entry point: allows writing `slot1 | slot2` as
      * the first step when building a row.
      */
    infix def |(next: Slot): BoardRow = Vector(slot, next)

  extension (position: BoardPosition)
    /** Checks that the position lies within the board's boundaries (non-negative row and column, each below `RowsCount`
      * and `ColsCount` respectively).
      */
    def isValid: Boolean =
      (position._1 < RowsCount && position._2 < ColsCount) && (position._1 >= 0 && position._2 >= 0)

  extension (row: BoardRow)
    /** Returns the `Slot` at the given column. */
    def apply(col: Int): Slot = row(col)

    /** Returns a new `BoardRow` with the `Slot` at the given column replaced. */
    def updated(col: Int, card: Slot): BoardRow = row.updated(col, card)

    /** Counts the number of occupied cells (cards present) in the row. */
    @targetName("rowNumberOfCards")
    def numberOfCards: Int = row.count(_.isDefined)

    /** Extends an existing `BoardRow` by appending a new `Slot` at the end. Validates at runtime that the resulting row
      * does not exceed the `ColsCount` limit.
      */
    infix def |(next: Slot): BoardRow =
      val updatedRow = row :+ next
      require(updatedRow.length <= ColsCount, s"A row cannot exceed $ColsCount slots")
      updatedRow

    /** Combines this row with a following one to form a two-row `Board`. Validates that both rows have exactly
      * `ColsCount` columns.
      */
    @targetName("rowConcat")
    infix def ||(nextRow: BoardRow): Board =
      require(row.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      require(nextRow.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      Vector(row, nextRow)

  extension (b: Board)
    /** Returns the `BoardRow` at the given row index. */
    def apply(row: Int): BoardRow = b(row)

    /** Returns a new `Board` with the `Slot` at the given position replaced. */
    def updatedSlot(position: BoardPosition, card: Slot): Board =
      val (row, col) = position
      b.updated(row, b(row).updated(col, card))

    /** Returns a new `Board` with the entire row at the given index replaced. */
    def updateRow(row: Int, updatedRow: BoardRow): Board =
      b.updated(row, updatedRow)

    /** Counts the total number of cards present on the whole board, summing across all rows. */
    @targetName("boardNumberOfCards")
    def numberOfCards: Int = b.map(_.numberOfCards).sum

    /** Appends a new row to an existing `Board`. Validates that the row has exactly `ColsCount` columns and that the
      * resulting board does not exceed the `RowsCount` limit.
      */
    @targetName("boardConcatRow")
    infix def ||(nextRow: BoardRow): Board =
      require(nextRow.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      val updated = b :+ nextRow
      require(updated.length <= RowsCount, s"A board cannot exceed $RowsCount rows")
      updated

  /** Factory for explicit construction of `BoardRow`, as an alternative to the `|`-based DSL. */
  object BoardRow:
    /** Returns an empty row, with all cells set to `None`. */
    def empty: BoardRow = Vector.fill(ColsCount)(None)

    /** Builds a `BoardRow` from an explicit sequence of `Slot`s. Requires the number of slots to match `ColsCount`
      * exactly.
      */
    def apply(slots: Slot*): BoardRow =
      require(slots.length == ColsCount, s"A row must contain exactly $ColsCount slots")
      slots.toVector

  /** Factory for explicit construction of `Board`, as an alternative to the `||`-based DSL. */
  object Board:
    /** Returns an empty board, made up of `RowsCount` empty rows. */
    def empty: Board = Vector.fill(RowsCount)(BoardRow.empty)

    /** Builds a `Board` from three explicit rows. */
    def apply(r0: BoardRow, r1: BoardRow, r2: BoardRow): Board = Vector(r0, r1, r2)
