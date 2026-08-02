package org.scryption.game.model

object PlayerHand:

  opaque type PlayerHand = List[Card[?]]

  def empty: PlayerHand = List.empty

  def fromList(cards: List[Card[?]]): PlayerHand = cards

  extension (hand: PlayerHand)

    def size: Int = hand.size

    def isEmpty: Boolean = hand.isEmpty

    def toList: List[Card[?]] = hand

    infix def addCard(card: Card[?]): PlayerHand = card :: hand

    infix def removeCard(card: Card[?]): PlayerHand = hand match
      case Nil => Nil
      case head :: tail if head.id == card.id => tail
      case head :: tail => head :: tail.removeCard(card)