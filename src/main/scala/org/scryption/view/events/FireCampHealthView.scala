package org.scryption.view.events

import org.scryption.GUIChannelInterface
import org.scryption.view.StatBonus

/** Fire camp event: pick a card, boost its health by 2. */
class FireCampHealthView(channel: GUIChannelInterface)
  extends EventView(
    channel = channel,
    cardWidth = 250,
    bonus = StatBonus.Health(2),
    slotBgImagePath = "slots/slot_statboost_health.png"
  )