package org.scryption.view

import org.scryption.{EventMessages, GameMessagesChannel, Trial}
import org.scryption.game.model.Card
import org.scryption.view.common.{CardViewInfo, cardToViewInfo}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ViewModelDeckEvent(channel: GameMessagesChannel):

  private var currentCards: List[Card[?]] = Nil

  def ListenForCardsFromTheModel(f: List[CardViewInfo] => Unit): Unit =
    Future {
      while (true) {
        val msg = channel.receiveFromGame
        msg match {
          case EventMessages.Cards(cards) =>
            currentCards = cards
            f(cards.map(_.cardToViewInfo))

          case EventMessages.SingleCard(card) =>
            currentCards = List(card)
            f(card.cardToViewInfo :: Nil)

          case EventMessages.End =>
            currentCards = Nil
            f(Nil)

          case _ =>
        }
      }
    }

  def sendTrialChoiceToModel(choice: Trial): Unit =
    channel.sendToGame(EventMessages.TrialChoice(choice))

  def sendCardToModel(index: Int = 0): Unit =
    val card =
      if currentCards.isEmpty then throw new IllegalStateException("Nessuna carta presente nel ViewModel.")
      else if index >= 0 && index < currentCards.length then currentCards(index)
      else if index >= currentCards.length then currentCards.last
      else currentCards.head

    channel.sendToGame(EventMessages.SingleCard(card))

  def getSingleCardInfo(message: EventMessages): CardViewInfo = message match
    case EventMessages.SingleCard(card) => card.cardToViewInfo
    case EventMessages.Cards(card :: _) => card.cardToViewInfo
    case _                              => CardViewInfo("", "", "", "")
