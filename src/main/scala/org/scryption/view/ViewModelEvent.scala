package org.scryption.view

import org.scryption.{EventMessages, GUIChannelInterface}
import org.scryption.game.model.Card
import org.scryption.view.common.{CardViewInfo, toViewInfo}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ViewModelEvent(channel: GUIChannelInterface):

  private var currentCards: List[Card[?]] = Nil

  def getCardsFromModel(f: List[CardViewInfo] => Unit): Unit =
    Future {
      while (true) {
        val msg = channel.receiveFromGame
        msg match {
          case EventMessages.Cards(cards) =>
            currentCards = cards
            f(cards.map(_.toViewInfo))
          case EventMessages.SingleCard(card) =>
            f(card.toViewInfo :: Nil)
          case EventMessages.End => f(Nil)
        }

      }
    }




  def getSingleCardInfo(message: EventMessages): CardViewInfo = message match
    case EventMessages.SingleCard(card) => card.toViewInfo
    case EventMessages.Cards(card :: _) => card.toViewInfo
    case _                                   => CardViewInfo("", "", "", "")

  def sendCardToModel(index: Int): Unit =

   val card =
     if currentCards.isEmpty then
         throw new IllegalStateException("Nessuna carta presente nel ViewModel.")
     else if index >= 0 && index < currentCards.length then
         currentCards(index)
     else if index >= currentCards.length then
         currentCards.last
     else
         currentCards.head

   channel.sendToGame(EventMessages.SingleCard(card))