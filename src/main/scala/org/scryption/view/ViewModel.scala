package org.scryption.view

import org.scryption.GUIMessages
import org.scryption.game.model.Card
import org.scryption.view.common.CardViewInfo
import org.scryption.view.common.toViewInfo

class ViewModel() {

  private var currentCards: List[Card[?]] = Nil

  def getCardsInfo(message: GUIMessages): List[CardViewInfo] = message match {
    case GUIMessages.Cards(cards)       => {
      currentCards = cards
      cards.map(_.toViewInfo)
    }
    case GUIMessages.SingleCard(card)   => card.toViewInfo :: Nil
    case _                              => Nil
  }

  def getSingleCardInfo(message: GUIMessages): CardViewInfo = message match {
    case GUIMessages.SingleCard(card) => card.toViewInfo
    case GUIMessages.Cards(card :: _) => card.toViewInfo
    case _                            => CardViewInfo("", "", "", "")
  }

  def getModelCard(index: Int): GUIMessages.SingleCard = index match {
    case i if (i >= 0) => currentCards.length match {
      case l if (l > i) => GUIMessages.SingleCard(currentCards(i))
      case l            => GUIMessages.SingleCard(currentCards(l - 1))
    }
  }
}
