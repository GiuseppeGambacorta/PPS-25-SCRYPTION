package org.scryption.game.model

import org.scryption.game.model.HitTarget.*
import org.scryption.game.model.boardModel.BoardRow

enum HitTarget:
  case Opponent
  case OpposingCard(colIndex: Int)

trait FightResolver:

  /** Gets the target of an attacking card.
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
