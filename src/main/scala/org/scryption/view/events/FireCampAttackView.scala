package org.scryption.view.events

import org.scryption.GUIChannelInterface
import org.scryption.view.ViewModelEvent
import org.scryption.view.common.StatBonus

class FireCampAttackView(viewModel : ViewModelEvent)
  extends EventView(
    viewModel = viewModel,
    cardWidth = 250,
    bonus = StatBonus.Attack(1),
    slotBgImagePath = "statboost_attack"
  )