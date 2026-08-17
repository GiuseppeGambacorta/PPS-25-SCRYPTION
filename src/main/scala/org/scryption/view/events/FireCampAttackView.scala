package org.scryption.view.events

import org.scryption.GUIChannelInterface
import org.scryption.view.common.StatBonus

class FireCampAttackView(channel: GUIChannelInterface)
  extends EventView(
    channel = channel,
    cardWidth = 250,
    bonus = StatBonus.Attack(1),
    slotBgImagePath = "statboost_attack"
  )