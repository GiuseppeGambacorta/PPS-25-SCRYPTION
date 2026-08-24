package org.scryption.view.events

import org.scryption.GameMessagesChannel
import org.scryption.view.ViewModelDeckEvent
import org.scryption.view.common.StatBonus

class FireCampHealthView(viewModel: ViewModelDeckEvent)
    extends FireCampView(
      viewModel = viewModel,
      cardWidth = 250,
      bonus = StatBonus.Health(2),
      slotBgImagePath = "statboost_health"
    )
