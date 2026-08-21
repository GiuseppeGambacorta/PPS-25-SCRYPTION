package org.scryption.game.model

import org.scryption.game.model.Rarity.Common
import org.scryption.game.model.SacrificeAttribute.Blood

import scala.util.Random

object Deck:

  /** An immutable list of heterogeneous cards.
   * */
  opaque type Deck = List[Card[?]]

  /** @return an empty deck.
   */
  def empty: Deck = List.empty

  /** @return a predefined standard deck starting configuration.
   */
  def getStandardDeck: Deck = {
    val stoat: Card[?] = CreatureCard.empty named "Stoat" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Fledgling addSeal Seal.Sprinter(Direction.Right) addSeal Seal.Hefty addSeal Seal.Leader addSeal Seal.BoneKing addSeal Seal.Burrower
    List(stoat, stoat, CardLibrary.bullfrog, CardLibrary.wolf, CardLibrary.wolf)
  }

  /** Wraps a list of cards into a Deck.
   *
   * @param cards The list of cards.
   * @return a new deck containing the provided cards.
   */
  def fromList(cards: List[Card[?]]): Deck = cards

  extension (deck: Deck)

    /** @return the number of cards currently in the deck.
     */
    def size: Int = deck.size

    /** @return true if the deck has no cards, false otherwise
     */
    def isEmpty: Boolean = deck.isEmpty

    /** Adds a single card to the top of the deck.
     *
     * @param card The card to add.
     * @return a new Deck instance with the added card.
     */
    infix def addCard(card: Card[?]): Deck = card :: deck

    /** Removes the specified card from the deck.
     *
     * @param card The card to remove.
     * @return a new Deck instance without the specified card.
     */
    infix def removeCard(card: Card[?]): Deck = deck match
      case Nil                                => Nil
      case head :: tail if head.id == card.id => tail
      case head :: tail                       => head :: tail.removeCard(card)

    /** Draws the top card from the deck.
     *
     * @return an option containing a tuple with the drawn card and
     * the remaining deck, or None if the deck is empty.
     */
    def draw: Option[(Card[?], Deck)] = deck match
      case Nil          => None
      case head :: tail => Some((head, tail))

    /** @return the deck representation as a standard List.
     */
    def toList: List[Card[?]] = deck

    /** Shuffles the deck using a random seed.
     *
     * @param seed Optional seed for reproducible shuffles.
     * @return a new shuffled Deck instance.
     */
    def shuffle(seed: Long = Random.nextLong()): Deck = new Random(seed).shuffle(deck)

    /** Draws a specified number of random cards from the deck.
     *
     * @param n The number of cards to draw.
     * @param seed Optional seed for reproducibility.
     * @return a tuple containing the list of drawn cards and the remaining deck.
     */
    def drawRandom(n: Int, seed: Long = Random.nextLong()): (List[Card[?]], Deck) =
      new Random(seed).shuffle(deck).splitAt(n)

    /** Adds a list of new cards to the top of the deck.
     *
     * @param newCards The cards to add.
     * @return A new Deck instance containing the added cards.
     */
    infix def addCards(newCards: List[Card[?]]): Deck = newCards ::: deck
