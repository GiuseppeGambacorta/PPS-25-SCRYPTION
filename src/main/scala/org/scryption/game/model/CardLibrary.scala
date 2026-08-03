package org.scryption.game.model

import org.scryption.game.model.SacrificeAttribute.{Blood, Bones}
import org.scryption.game.model.Seal.*

import scala.util.Random

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

  def mantisGod: Card[?] =
    CreatureCard.empty named "Mantis God" withAttack 1 withHealth 1 withSacrificeAttribute Blood(1) addSeal TrifurcatedStrike withRarity Rarity.Rare

  def urayuli: Card[?] =
    CreatureCard.empty named "Urayuli" withAttack 7 withHealth 7 withSacrificeAttribute Blood(4) withRarity Rarity.Rare

  def getADeckWithAllTheLibrary: Deck.Deck = Deck.fromList(List(squirrel, bear))

  private val commonCards: List[() => Card[?]] = List(
    () => squirrel, () => bear, () => stoat, () => wolf, () => sparrow, () => adder, () => mantis, () => opossum
  )

  private val rareCards: List[() => Card[?]] = List(
    () => mantisGod, () => urayuli
  )

  /** Generates N random cards based on the rarity requested
   * @param rarity The rarity required.
   * @param n The number of cards required to be generated.
   * @param seed Optional seed for reproducible shuffles.
   * @return a list with the generated cards*/
  def generateRandomCards(rarity: Rarity, n: Int, seed: Long = Random.nextLong()): List[Card[?]] =
    val pool = rarity match
      case Rarity.Common => commonCards
      case Rarity.Rare => rareCards
    val random = new Random(seed)
    random.shuffle(pool).take(n).map(factory => factory())

