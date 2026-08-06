package org.scryption.game.model

import org.scryption.game.model.HitTarget.*
import org.scryption.game.model.boardModel.{BoardRow, ColsCount}

enum HitTarget:
  case Opponent
  case OpposingCard(colIndex: Int)

trait FightResolver:

  /** Gets the target of an attacking card.
   * 
   * @param attackerCol The column of the attacker
   * @param attacker The attacking card
   * @param opponentRow The row in front of the attacking card
   * @return the targets list to hit
   */
  def getTargets(attackerCol: Int, attacker: Card[?], opponentRow: BoardRow): List[HitTarget]

class BasicResolver extends FightResolver:
  def getTargets(attackerCol: Int, attacker: Card[?], opponentRow: BoardRow): List[HitTarget] =
    opponentRow(attackerCol) match
      case Some(_) => List(OpposingCard(attackerCol))
      case None => List(Opponent)

trait AirborneResolver extends FightResolver:
  abstract override def getTargets(attackerCol: Int, attacker: Card[?], opponentRow: BoardRow): List[HitTarget] =
    attacker.seals match
      case seals if !seals.contains(Seal.Airborne) =>
        super.getTargets(attackerCol, attacker, opponentRow)
      case _ =>
        opponentRow(attackerCol) match
          case Some(card) if card.seals.contains(Seal.MightyLeap) =>
            super.getTargets(attackerCol, attacker, opponentRow)
          case _ => List(Opponent)

trait StrikeResolver extends FightResolver:
  abstract override def getTargets(attackerCol: Int, attacker: Card[?], opponentRow: BoardRow): List[HitTarget] =
    attacker.seals match
      case seals if seals.contains(Seal.TrifurcatedStrike) =>
        val targetCols = List(attackerCol - 1, attackerCol, attackerCol + 1).filter(c => c >= 0 && c < ColsCount)
        targetCols.flatMap(col => super.getTargets(col, attacker, opponentRow))
      case seals if seals.contains(Seal.BifurcatedStrike) =>
        val targetCols = List(attackerCol - 1, attackerCol + 1).filter(c => c >= 0 && c < ColsCount)
        targetCols.flatMap(col => super.getTargets(col, attacker, opponentRow))
      case _ => super.getTargets(attackerCol, attacker, opponentRow)
