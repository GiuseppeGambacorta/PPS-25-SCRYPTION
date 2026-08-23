package org.scryption.view.events

import org.scryption.GameMessagesChannel
import org.scryption.view.ViewModelDeckEvent
import org.scryption.view.common.StatBonus

/** Fire camp event: pick a card, boost its health by 2. */
class FireCampHealthView(viewModel: ViewModelDeckEvent)
    extends EventView(
      viewModel = viewModel,
      cardWidth = 250,
      bonus = StatBonus.Health(2),
      slotBgImagePath = "statboost_health"
    )
