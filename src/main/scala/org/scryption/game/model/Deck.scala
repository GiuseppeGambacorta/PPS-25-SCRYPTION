package org.scryption.game.model

import scala.annotation.tailrec
import scala.util.Random

object Deck:

  opaque type Deck = List[Card]

  def empty: Deck = List.empty

  def getStandardDeck: Deck = List(CardLibrary.squirrel, CardLibrary.squirrel)

  def fromList(cards: List[Card]): Deck = cards

  extension (deck: Deck)

    def size: Int = deck.size

    def isEmpty: Boolean = deck.isEmpty

    infix def addCard(card: Card): Deck = card :: deck

    infix def removeCard(card: Card): Deck = deck match
      case Nil => Nil
      case head :: tail if head == card => tail
      case head :: tail => head :: tail.removeCard(card)

    def draw: Option[(Card, Deck)] = deck match
      case Nil => None
      case head :: tail => Some((head, tail))

    def toList: List[Card] = deck

    def shuffle(seed: Long = Random.nextLong()): Deck = new Random(seed).shuffle(deck)