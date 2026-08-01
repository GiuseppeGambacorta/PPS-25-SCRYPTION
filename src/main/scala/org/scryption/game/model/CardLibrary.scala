package org.scryption.game.model

object CardLibrary:

  def squirrel: Card[?] = CreatureCard.empty withAttack 0 named "Squirrel" withHealth 1
  def bear: Card[?] = CreatureCard.empty withAttack 2 named "Bear" withHealth 3
  
  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, bear))
