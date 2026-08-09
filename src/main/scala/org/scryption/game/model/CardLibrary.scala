package org.scryption.game.model

import org.scryption.game.model.SacrificeAttribute.*
import org.scryption.game.model.Rarity.*

object CardLibrary:

  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, sparrow, raven, turkeyVulture, bloodhound, wolf, coyote, elk, pronghorn, bee, mantis, ringWorm, geck, adder, riverSnapper, rattler, stoat, grizzly, opossum, bat))
  def getADeckWithRareCards: Deck.Deck = Deck.fromList(List(mantisGod, ouroboros, moleMan, packRat, urayuli))
  def getDeckWithBasicCards: Deck.Deck = Deck.fromList(List(ravenEgg, sparrow, wolfCub, coyote, elkFawn, ringWorm, geck, bullfrog, riverSnapper, cat, bat, opossum))
  def getDeckWithAverageCards: Deck.Deck = Deck.fromList(List(kingfisher, raven, turkeyVulture, bloodhound, wolf, alpha, blackGoat, elk, pronghorn, beehive, mantis, cockroach, adder, rattler, mole, porcupine, riverOtter, warren, beaver, fieldMice))
  def getDeckWithAdvancesCards: Deck.Deck = Deck.fromList(List(mooseBuck, grizzly, greatWhite))

  // Avian family

  val kingfisher: Card[?] = CreatureCard.empty named "Kingfisher" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Waterborne addSeal Seal.Airborne
  val ravenEgg: Card[?] = CreatureCard.empty named "Raven Egg" withAttack 0 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Fledgling
  val sparrow: Card[?] = CreatureCard.empty named "Sparrow" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Airborne
  val raven: Card[?] = CreatureCard.empty named "Raven" withAttack 2 withHealth 3 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.Airborne
  val turkeyVulture: Card[?] = CreatureCard.empty named "Turkey Vulture" withAttack 3 withHealth 3 withSacrificeAttribute Bones(8) withRarity Common addSeal Seal.Airborne

  // Canine family

  val wolfCub: Card[?] = CreatureCard.empty named "Wolf Cub" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Fledgling
  val bloodhound: Card[?] = CreatureCard.empty named "Bloodhound" withAttack 2 withHealth 3 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.Guardian
  val wolf: Card[?] = CreatureCard.empty named "Wolf" withAttack 3 withHealth 2 withSacrificeAttribute Blood(2) withRarity Common
  val coyote: Card[?] = CreatureCard.empty named "Coyote" withAttack 2 withHealth 1 withSacrificeAttribute Bones(4) withRarity Common
  val alpha: Card[?] = CreatureCard.empty named "Alpha" withAttack 1 withHealth 2 withSacrificeAttribute Bones(4) withRarity Common addSeal Seal.Leader

  // Hooved family

  val blackGoat: Card[?] = CreatureCard.empty named "Black Goat" withAttack 0 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.WorthySacrifice
  val elkFawn: Card[?] = CreatureCard.empty named "Elk Fawn" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Fledgling addSeal Seal.Sprinter
  val elk: Card[?] = CreatureCard.empty named "Elk" withAttack 2 withHealth 4 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.Sprinter
  val pronghorn: Card[?] = CreatureCard.empty named "Pronghorn" withAttack 1 withHealth 3 withSacrificeAttribute Blood(2) addSeal Seal.Sprinter addSeal Seal.BifurcatedStrike
  val mooseBuck: Card[?] = CreatureCard.empty named "Moose Buck" withAttack 3 withHealth 7 withSacrificeAttribute Blood(3) withRarity Common addSeal Seal.Hefty

  // Insect family

  val mantisGod: Card[?] = CreatureCard.empty named "Mantis God" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Rare addSeal Seal.TrifurcatedStrike
  val bee: Card[?] = CreatureCard.empty named "Bee" withAttack 1 withHealth 1 withRarity Common addSeal Seal.Airborne
  val beehive: Card[?] = CreatureCard.empty named "Beehive" withAttack 0 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.BeesWithin
  val mantis: Card[?] = CreatureCard.empty named "Mantis" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.BifurcatedStrike
  val ringWorm: Card[?] = CreatureCard.empty named "Ring Worm" withAttack 0 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common
  // Worker Ant Attack ?
  // Ant Queen Attack ?
  val cockroach: Card[?] = CreatureCard.empty named "Cockroach" withAttack 1 withHealth 1 withSacrificeAttribute Bones(4) withRarity Common addSeal Seal.Unkillable
  
  // Reptile family
  
  val geck: Card[?] = CreatureCard.empty named "Geck" withAttack 1 withHealth 1 withRarity Common
  val ouroboros: Card[?] = CreatureCard.empty named "Ouroboros" withAttack 1 withHealth 1 withSacrificeAttribute Blood(2) withRarity Rare addSeal Seal.Unkillable
  val bullfrog: Card[?] = CreatureCard.empty named "Bullfrog" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.MightyLeap
  val adder: Card[?] = CreatureCard.empty named "Adder" withAttack 1 withHealth 1 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.TouchOfDeath
  val riverSnapper: Card[?] = CreatureCard.empty named "River Snapper" withAttack 1 withHealth 6 withSacrificeAttribute Blood(2) withRarity Common
  val rattler: Card[?] = CreatureCard.empty named "Rattler" withAttack 3 withHealth 1 withSacrificeAttribute Bones(6) withRarity Common
  
  // No family
  
  val moleMan: Card[?] = CreatureCard.empty named "Mole Man" withAttack 0 withHealth 6 withSacrificeAttribute Blood(1) withRarity Rare addSeal Seal.MightyLeap addSeal Seal.Burrower
  val packRat: Card[?] = CreatureCard.empty named "Pack Rat" withAttack 2 withHealth 2 withSacrificeAttribute Blood(2) withRarity Rare addSeal Seal.TrinketBearer
  val urayuli: Card[?] = CreatureCard.empty named "Urayuli" withAttack 7 withHealth 7 withSacrificeAttribute Blood(4) withRarity Rare
  
  val squirrel: Card[?] = CreatureCard.empty named "Squirrel" withAttack 0 withHealth 1 withRarity Common
  val rabbit: Card[?] = CreatureCard.empty named "Rabbit" withAttack 0 withHealth 1 withRarity Common
  val cat: Card[?] = CreatureCard.empty named "Cat" withAttack 0 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.ManyLives
  val mole: Card[?] = CreatureCard.empty named "Mole" withAttack 0 withHealth 4 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Burrower
  val porcupine: Card[?] = CreatureCard.empty named "Porcupine" withAttack 1 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.SharpQuills
  val riverOtter: Card[?] = CreatureCard.empty named "River Otter" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.Waterborne
  val stoat: Card[?] = CreatureCard.empty named "Stoat" withAttack 1 withHealth 3 withSacrificeAttribute Blood(1) withRarity Common
  val warren: Card[?] = CreatureCard.empty named "Warren" withAttack 0 withHealth 2 withSacrificeAttribute Blood(1) withRarity Common addSeal Seal.RabbitHole
  val beaver: Card[?] = CreatureCard.empty named "Beaver" withAttack 1 withHealth 3 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.DamBuilder
  val fieldMice: Card[?] = CreatureCard.empty named "Field Mice" withAttack 2 withHealth 2 withSacrificeAttribute Blood(2) withRarity Common addSeal Seal.Fecundity
  val greatWhite: Card[?] = CreatureCard.empty named "Great White" withAttack 4 withHealth 2 withSacrificeAttribute Blood(3) withRarity Common addSeal Seal.Waterborne
  val grizzly: Card[?] = CreatureCard.empty named "Grizzly" withAttack 4 withHealth 6 withSacrificeAttribute Blood(3) withRarity Common
  val opossum: Card[?] = CreatureCard.empty named "Opossum" withAttack 1 withHealth 1 withSacrificeAttribute Bones(2) withRarity Common
  val bat: Card[?] = CreatureCard.empty named "Bat" withAttack 2 withHealth 1 withSacrificeAttribute Bones(4) withRarity Common
  
  // Support
  
  val boulder: Card[?] = SupportCard.empty named "Boulder" withHealth 5
  val dam: Card[?] = SupportCard.empty named "Dam" withHealth 2
  val frozenOpossum: Card[?] = SupportCard.empty named "Frozen Opossum" withHealth 5
  val grandFir: Card[?] = SupportCard.empty named "Grand Fir" withHealth 3 addSeal Seal.MightyLeap
  val stump: Card[?] = SupportCard.empty named "Stump" withHealth 3

  val allCards: List[Card[?]] = List(
    kingfisher, ravenEgg, sparrow, raven, turkeyVulture,
    wolfCub, bloodhound, wolf, coyote, alpha,
    blackGoat, elkFawn, elk, pronghorn, mooseBuck,
    mantisGod, bee, beehive, mantis, ringWorm, cockroach,
    geck, ouroboros, bullfrog, adder, riverSnapper, rattler,
    moleMan, packRat, urayuli, squirrel, rabbit, cat, mole,
    porcupine, riverOtter, stoat, warren, beaver, fieldMice,
    greatWhite, grizzly, opossum, bat,
    boulder, dam, frozenOpossum, grandFir, stump
  )

  def byName(name: String): Option[Card[?]] =
    allCards.find(_.name == name)
  
