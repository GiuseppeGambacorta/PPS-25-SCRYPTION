package org.scryption.view.events

import org.scryption.GUIChannelInterface
import org.scryption.view.StatBonus

/** Fire camp event: pick a card, boost its attack by 1. */
class FireCampAttackView(channel: GUIChannelInterface)
  extends EventView(
    channel = channel,
    cardWidth = 380,
    bonus = StatBonus.Attack(1),
    slotBgImagePath = "slots/slot_statboost_attack.png"
  )