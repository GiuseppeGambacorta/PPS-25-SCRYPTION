package org.scryption.game.model

import org.scryption.game.model.SacrificeAttribute.*

object CardLibrary:

  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, sparrow, raven, turkeyVulture, bloodhound, wolf, coyote, elk, pronghorn, bee, mantis, ringWorm, geck, adder, riverSnapper, rattler, stoat, grizzly, opossum, bat))
  val squirrel: Card[?] = CreatureCard.empty named "Squirrel" withAttack 0 withHealth 1
  val sparrow: Card[?] = CreatureCard.empty named "Sparrow" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) addSeal Seal.Airborne
  val raven: Card[?] = CreatureCard.empty named "Raven" withAttack 2 withHealth 3 withSacrificeAttribute Blood(2) addSeal Seal.Airborne
  val turkeyVulture: Card[?] = CreatureCard.empty named "Turkey Vulture" withAttack 3 withHealth 3 withSacrificeAttribute Bones(8) addSeal Seal.Airborne
  val bloodhound: Card[?] = CreatureCard.empty named "Bloodhound" withAttack 2 withHealth 3 withSacrificeAttribute Blood(2) addSeal Seal.Guardian
  val wolf: Card[?] = CreatureCard.empty named "Wolf" withAttack 3 withHealth 2 withSacrificeAttribute Blood(2)
  val coyote: Card[?] = CreatureCard.empty named "Coyote" withAttack 2 withHealth 1 withSacrificeAttribute Bones(4)
  val elk: Card[?] = CreatureCard.empty named "Elk" withAttack 2 withHealth 4 withSacrificeAttribute Blood(2) addSeal Seal.Sprinter
  val pronghorn: Card[?] = CreatureCard.empty named "Pronghorn" withAttack 1 withHealth 3 withSacrificeAttribute Blood(2) addSeal Seal.Sprinter addSeal Seal.BifurcatedStrike
  val bee: Card[?] = CreatureCard.empty named "Bee" withAttack 1 withHealth 1 addSeal Seal.Airborne
  val mantis: Card[?] = CreatureCard.empty named "Mantis" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) addSeal Seal.BifurcatedStrike
  val ringWorm: Card[?] = CreatureCard.empty named "Ring Worm" withAttack 0 withHealth 1 withSacrificeAttribute Blood(1)
  val geck: Card[?] = CreatureCard.empty named "Geck" withAttack 1 withHealth 1
  val adder: Card[?] = CreatureCard.empty named "Adder" withAttack 1 withHealth 1 withSacrificeAttribute Blood(2) addSeal Seal.TouchOfDeath
  val riverSnapper: Card[?] = CreatureCard.empty named "River Snapper" withAttack 1 withHealth 6 withSacrificeAttribute Blood(2)
  val rattler: Card[?] = CreatureCard.empty named "Rattler" withAttack 3 withHealth 1 withSacrificeAttribute Bones(6)
  val stoat: Card[?] = CreatureCard.empty named "Stoat" withAttack 1 withHealth 3 withSacrificeAttribute Blood(1)
  val grizzly: Card[?] = CreatureCard.empty named "Grizzly" withAttack 4 withHealth 6 withSacrificeAttribute Blood(3)
  val opossum: Card[?] = CreatureCard.empty named "Opossum" withAttack 1 withHealth 1 withSacrificeAttribute Bones(2)
  val bat: Card[?] = CreatureCard.empty named "Bat" withAttack 2 withHealth 1 withSacrificeAttribute Bones(4)
  val bear: Card[?] = CreatureCard.empty named "Bear" withAttack 2 withHealth 3
