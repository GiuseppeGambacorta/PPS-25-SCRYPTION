package org.scryption.game.model

import org.scryption.game.model.SacrificeAttribute.{Blood, Bones}
import org.scryption.game.model.Seal.*

object CardLibrary:

  def squirrel: Card[?] = CreatureCard.empty named "Squirrel" withAttack 0 withHealth 1

  def bear: Card[?] = CreatureCard.empty named "Bear" withAttack 2 withHealth 3 withSacrificeAttribute Blood(3)

  def stoat: Card[?] = CreatureCard.empty named "Stoat" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1)

  def wolf: Card[?] = CreatureCard.empty named "Wolf" withAttack 3 withHealth 2 withSacrificeAttribute Blood(2)

  def sparrow: Card[?] =
    CreatureCard.empty named "Sparrow" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) addSeal Airborne

  def adder: Card[?] =
    CreatureCard.empty named "Adder" withAttack 1 withHealth 1 withSacrificeAttribute Blood(2) addSeal TouchOfDeath

  def mantis: Card[?] =
    CreatureCard.empty named "Mantis" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) addSeal BifurcatedStrike

  def opossum: Card[?] =
    CreatureCard.empty named "Opossum" withAttack 1 withHealth 1 withSacrificeAttribute Bones(2)

  def boulder: Card[?] =
    SupportCard.empty named "Boulder" withHealth 5 addSeal Wall

  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, bear))
