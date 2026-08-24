package org.scryption.view.common

import org.scryption.view.common.CardViewInfo

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

  final case class Merge(duplicateCard: CardViewInfo) extends StatBonus {
    def apply(info: CardViewInfo): CardViewInfo =
      info.copy(
        attack = (info.attack.toInt + duplicateCard.attack.toInt).toString,
        health = (info.health.toInt + duplicateCard.health.toInt).toString,
        addedSigils = info.addedSigils.concat(duplicateCard.defaultSigils).concat(duplicateCard.addedSigils)
      )
  }

  final case class Sigils(newSigils: List[String]) extends StatBonus {
    def apply(info: CardViewInfo): CardViewInfo = info.copy(addedSigils = info.addedSigils.concat(newSigils))
  }
}
