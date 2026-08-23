package org.scryption.game.model

import org.scryption.game.model.Deck.Deck

case class DrawDecks(mainDeck: Deck):

  /** Draws a card from the main deck.
   *
   * @return a card and the updated deck without it.
   */
  def drawFromMain(): Option[(Card[?], DrawDecks)] = mainDeck.draw match
    case None => None
    case Some(card, deck) => Some(card, this.copy(mainDeck = deck))

  /** Draws a card from the infinite squirrel deck.
   *
   * @return a squirrel card.
   */
  def drawFromSquirrels: Card[?] =
    CardLibrary.squirrel
