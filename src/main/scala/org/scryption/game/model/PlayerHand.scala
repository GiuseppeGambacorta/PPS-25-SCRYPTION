package org.scryption.game.model

object PlayerHand:

  opaque type PlayerHand = List[Card[?]]

  /** @return
    *   an empty hand.
    */
  def empty: PlayerHand = List.empty

  /** Wraps a list of cards into a PlayerHand.
    *
    * @param cards
    *   The list of cards.
    * @return
    *   a new PlayerHand containing the provided cards.
    */
  def fromList(cards: List[Card[?]]): PlayerHand = cards

  extension (hand: PlayerHand)

    def size: Int = hand.size

    def isEmpty: Boolean = hand.isEmpty

    def toList: List[Card[?]] = hand

    /** Adds a single card to the top of the hand.
      *
      * @param card
      *   The card to add.
      * @return
      *   a new PlayerHand instance with the added card.
      */
    infix def addCard(card: Card[?]): PlayerHand = card :: hand

    /** Adds a list of new cards to the player's hand.
      *
      * @param cards
      *   The list of cards to add.
      * @return
      *   a new PlayerHand instance containing the added cards.
      */
    infix def addCards(cards: List[Card[?]]): PlayerHand = cards ::: hand

    /** Removes the specified card from the hand.
      *
      * @param card
      *   The card to remove.
      * @return
      *   a new PlayerHand instance without the specified card.
      */
    infix def removeCard(card: Card[?]): PlayerHand = hand match
      case Nil                                => Nil
      case head :: tail if head.id == card.id => tail
      case head :: tail                       => head :: tail.removeCard(card)

    /** Extract a specific card from the hand.
      *
      * @param card
      *   The card to extract.
      * @return
      *   an Option containing a tuple with the card extracted and the new hand. Returns None if the card is not
      *   present.
      */
    def extractCard(card: Card[?]): Option[(Card[?], PlayerHand)] =
      hand.find(_.id == card.id).map(card => (card, hand.removeCard(card)))
