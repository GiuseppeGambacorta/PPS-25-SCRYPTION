package org.scryption.game.model

import org.scryption.game.model.Deck.Deck

case class DrawDecks(mainDeck: Deck):

  def drawFromMain(): Option[(Card[?], DrawDecks)] = mainDeck.draw match
    case None => None
    case Some(card, deck) => Some(card, this.copy(mainDeck = deck))

  def drawFromSquirrels: Card[?] =
    CardLibrary.squirrel
