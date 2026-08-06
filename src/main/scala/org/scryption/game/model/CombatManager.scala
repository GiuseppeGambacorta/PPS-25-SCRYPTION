package org.scryption.game.model

import org.scryption.game.model.boardModel.BoardRow

/** Represents the result of a single card's attack phase.
 */
case class CombatResult(updatedRow: BoardRow, damageToOpponent: Int, killedCards: List[Card[?]])

object CombatManager:

  /** Performs an attack from a single card, resolving targets and applying damage.
   *
   * @param attackerCol The column index of the attacking card.
   * @param attacker The attacking card.
   * @param opponentRow The current composition of the opponent's front row
   * @param resolver The FightResolver to use for targeting.
   * @return a CombatResult containing the new row state and damage dealt to the opponent
   */
  def executeAttack(attackerCol: Int,
                    attacker: Card[?],
                    opponentRow: BoardRow,
                    resolver: FightResolver): CombatResult =
    val attackDamage = attacker match
      case creature: CreatureCard => creature.attack
      case _: SupportCard         => 0
    val targets = resolver.getTargets(attackerCol, attacker, opponentRow)
    targets.foldLeft(CombatResult(opponentRow, 0, List.empty)) { (currentResult, target) => target match
      case HitTarget.Opponent =>
        currentResult.copy(damageToOpponent = currentResult.damageToOpponent + attackDamage)
      case HitTarget.OpposingCard(colIndex) =>
        currentResult.updatedRow(colIndex) match
          case Some(targetCard) =>
            val hasTouchOfDeath = attacker.seals.contains(Seal.TouchOfDeath) && attackDamage > 0
            val remainingHealth = targetCard.health - attackDamage
            if hasTouchOfDeath || remainingHealth <= 0 then
              currentResult.copy(
                updatedRow = currentResult.updatedRow.updated(colIndex, boardModel.x),
                killedCards = currentResult.killedCards :+ targetCard
              )
            else
              val updatedTargetCard = targetCard withHealth remainingHealth
              currentResult.copy(updatedRow = currentResult.updatedRow.updated(colIndex, Some(updatedTargetCard)))
          case None => currentResult
    }
