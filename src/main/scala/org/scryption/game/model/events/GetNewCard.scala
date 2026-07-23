package org.scryption.game.model.events

import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.{Card, CardLibrary, Deck}

trait Channel:
  def setCards(deck : List[Card]) : Unit
  def getCards : List[Card]
  def setSelectedOne(card: Card) : Unit
  def getSelectedOne: Option[Card]
  def isReady : Boolean


class channelImpl extends Channel:
  private var cards: List[Card] = List()
  private var selectedCard: Option[Card] = None

  def setCards(deck : List[Card]) : Unit = {
    cards = deck
    selectedCard = None
  }

  def getCards : List[Card] = cards

  def setSelectedOne(c : Card) :Unit = if cards.contains(c) then selectedCard = Some(c) else selectedCard = None
  def getSelectedOne: Option[Card] = selectedCard

  def isReady : Boolean = selectedCard.isDefined



trait Event:
  def start() : Unit
  def isDone: Boolean
  def getResult : Deck

class GetNewCard(deck : Deck, ch : Channel) extends Event:

  override def start() : Unit = ch.setCards(List(CardLibrary.squirrel, CardLibrary.squirrel, CardLibrary.squirrel))

  override def isDone: Boolean = ch.isReady
  override def getResult : Deck = deck addCard ch.getSelectedOne.get



@main def main =
  val ch = new channelImpl
  val event = new GetNewCard(Deck.getStandardDeck, ch)

  event.start()
  while !event.isDone do
    val cards = ch.getCards
    ch.setSelectedOne(cards.head)

  val newdeck = event.getResult
  println(newdeck)

