package org.scryption.view

import org.scryption.view.events.EventView

/** The stat bonus applied when a card is confirmed inside an [[EventView]] slot.
 *  Replaces the copy-pasted `visualAttackBonus`/`visualHealthBonus` field plus the
 *  `info.copy(attack = ...)` / `info.copy(health = ...)` line that differed between
 *  Strange Stones and the two fire camp views.
 */
sealed trait StatBonus {
  def apply(info: CardViewInfo): CardViewInfo
}

object StatBonus {
  final case class Attack(amount: Int) extends StatBonus {
    def apply(info: CardViewInfo): CardViewInfo = info.copy(attack = (info.attack.toInt + amount).toString)
  }

  final case class Health(amount: Int) extends StatBonus {
    def apply(info: CardViewInfo): CardViewInfo = info.copy(health = (info.health.toInt + amount).toString)
  }
}
