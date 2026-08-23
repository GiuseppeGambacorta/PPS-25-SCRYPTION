package org.scryption.view.events

import org.scryption.GameMessagesChannel
import org.scryption.view.ViewModelDeckEvent
import org.scryption.view.common.StatBonus

class FireCampAttackView(viewModel: ViewModelDeckEvent)
    extends EventView(
      viewModel = viewModel,
      cardWidth = 250,
      bonus = StatBonus.Attack(1),
      slotBgImagePath = "statboost_attack"
    )
