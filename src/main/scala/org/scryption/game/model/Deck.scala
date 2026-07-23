package org.scryption.game.model

//case class deck2 (cards: List[Card])

/*
trait Deck:
  type cards
  def addCard(c : Card): Unit

  def getDeck(): cards


object Deck:


  private class DeckImpl extends Deck:
    type cards = List[Card]

    private var cards: cards = List()

    def addCard(c : Card): Unit = cards = c :: cards

    def getDeck(): cards = cards


  def StandardDeck(): Deck = DeckImpl()


 */



object Deck :

 opaque type Deck = List[Card]

 def getStandardDeck: Deck = (List(CardLibrary.squirrel, CardLibrary.squirrel))
  extension (deck : Deck)

    infix def removeCard(c :Card): Deck = deck.filter(_ != c)

    infix def addCard(c :Card): Deck = c :: deck





@main def main2 : Unit =
  import Card.*


  val standardDeck = Deck.getStandardDeck
  println(standardDeck)

  val megasquirrel = CardLibrary.squirrel WithAttack 20 Named "MegaSquirell"
  val newdeck2 = standardDeck addCard megasquirrel
  println(newdeck2)

  val newdeck3 = standardDeck removeCard megasquirrel
  println(newdeck3)




