package org.scryption.game.model

import org.scryption.game.model.SacrificeAttribute.Blood


enum Seal:
  case Airborne
  case Wall
  case BifurcatedStrike
  case TrifurcatedStrike
  case TouchOfDeath
  case Guardian
  case Sprinter
  case Immortal
  case BoneKing
  case InfiniteSacrifice


enum SacrificeAttribute:
  case Blood(value: Int)
  case Bones(value: Int)
  case Nil()

case class Card(name: String, attack: Int, life: Int, sacrificeAttribute: SacrificeAttribute, seals : Set[Seal])


object Card:


  def card: Card =  Card("", 0, 0, SacrificeAttribute.Nil(), Set())

  extension (card: Card)

    infix def Named(name: String): Card =
      card.copy(name = name)

    infix def WithAttack(attack: Int): Card = {
      assert(attack >= 0)
      card.copy(attack = attack)
    }

    infix def WithLife(life: Int): Card =
      card.copy(life = life)

    infix def WithSacrifice(sacrificeAttribute: SacrificeAttribute): Card =
      card.copy(sacrificeAttribute = sacrificeAttribute)

    infix def AddSeal(seal: Seal): Card =
      card.copy(seals = card.seals + seal)

  @main def main: Unit =
    import Card.*


    val card3 = card AddSeal Seal.Sprinter
    val card4 = card Named "Test Card 4" WithAttack -10 WithLife 10 WithSacrifice Blood(10)


    println(card3)
    println(card4)