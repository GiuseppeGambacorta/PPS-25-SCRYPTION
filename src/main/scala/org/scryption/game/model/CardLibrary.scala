package org.scryption.game.model

object CardLibrary:

  val squirrel: Card = CreatureCard.empty named "Squirrel" withAttack 0 withHealth 1
  val bear: Card = CreatureCard.empty named "Bear" withAttack 2 withHealth 3
  
  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, bear))
