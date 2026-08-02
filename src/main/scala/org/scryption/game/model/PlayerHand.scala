package org.scryption.game.model

object PlayerHand:

  opaque type PlayerHand = List[Card[?]]

  def empty: PlayerHand = List.empty

  extension (hand: PlayerHand)

    def size: Int = hand.size

    def isEmpty: Boolean = hand.isEmpty