package org.scryption.view

import org.scryption.EventMessages
import org.scryption.game.model.Card
import org.scryption.view.common.{CardViewInfo, toViewInfo}

class ViewModel:

  private var currentCards: List[Card[?]] = Nil

  def getCardsInfo(message: EventMessages): List[CardViewInfo] = message match
    case EventMessages.Cards(cards) =>
      currentCards = cards
      cards.map(_.toViewInfo)
    case EventMessages.SingleCard(card) =>
      card.toViewInfo :: Nil
    case EventMessages.End =>
      Nil

  def getSingleCardInfo(message: EventMessages): CardViewInfo = message match
    case EventMessages.SingleCard(card) => card.toViewInfo
    case EventMessages.Cards(card :: _) => card.toViewInfo
    case _                                   => CardViewInfo("", "", "", "")

  def getModelCard(index: Int): EventMessages.SingleCard =
    if currentCards.isEmpty then
      throw new IllegalStateException("Nessuna carta presente nel ViewModel.")
    else if index >= 0 && index < currentCards.length then
      EventMessages.SingleCard(currentCards(index))
    else if index >= currentCards.length then
      EventMessages.SingleCard(currentCards.last)
    else
      EventMessages.SingleCard(currentCards.head)