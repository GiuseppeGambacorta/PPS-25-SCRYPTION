package org.scryption.game.model

object PlayerHand:

  opaque type PlayerHand = List[Card[?]]

  def empty: PlayerHand = List.empty

  extension (hand: PlayerHand)

    def size: Int = hand.size

    def isEmpty: Boolean = hand.isEmpty

    def toList: List[Card[?]] = hand

    infix def addCard(card: Card[?]): PlayerHand = card :: hand