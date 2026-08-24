package org.scryption.view

import org.scryption.game.model.Card
import org.scryption.game.model.items.GameItem
import org.scryption.view.common.{CardViewInfo, ItemViewInfo, cardToViewInfo, itemToViewInfo}
import org.scryption.{EventMessages, GameMessagesChannel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewModelItemEvent(channel: GameMessagesChannel):

  private var currentItems: List[GameItem] = Nil
  private var currentCard: Option[Card[?]] = None

  def listenForEvents(
      onItems: List[ItemViewInfo] => Unit,
      onCardFallback: CardViewInfo => Unit
  ): Unit =
    Future {
      while true do
        val msg = channel.receiveFromGame
        msg match
          case EventMessages.Items(items) =>
            currentItems = items
            currentCard = None
            onItems(items.map(_.itemToViewInfo))

          case EventMessages.SingleItem(item) =>
            currentItems = List(item)
            currentCard = None
            onItems(List(item.itemToViewInfo))

          case EventMessages.SingleCard(card) =>
            currentItems = Nil
            currentCard = Some(card)
            onCardFallback(card.cardToViewInfo)

          case EventMessages.End =>
            currentItems = Nil
            currentCard = None
            onItems(Nil)

          case _ =>
    }

  def sendItemToModel(index: Int): Unit =
    val item =
      if currentItems.isEmpty then throw new IllegalStateException("Nessun item presente nel ViewModel.")
      else if index >= 0 && index < currentItems.length then currentItems(index)
      else if index >= currentItems.length then currentItems.last
      else currentItems.head

    channel.sendToGame(EventMessages.SingleItem(item))

  def sendCardToModel(): Unit =
    val card = currentCard.getOrElse(
      throw new IllegalStateException("Nessuna carta presente nel ViewModel.")
    )
    channel.sendToGame(EventMessages.SingleCard(card))
