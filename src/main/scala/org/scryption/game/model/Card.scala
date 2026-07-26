package org.scryption.game.model

import org.scryption.game.model.Rarity.Common

sealed trait Card:
  def name: String
  def health: Int
  def sacrificeAttribute: SacrificeAttribute
  def seals: Set[Seal]
  def rarity: Rarity
  def named(name: String): Card
  def withHealth(health: Int): Card
  def addSeal(seal: Seal): Card
  def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): Card

enum SacrificeAttribute:
  case Blood(value: Int)
  case Bones(value: Int)
  case Nil()

  def isValid: Boolean = this match
    case Blood(v) => v >= 0
    case Bones(v) => v >= 0
    case Nil()    => true

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

enum Rarity:
  case Common
  case Rare

object CreatureCard:
  def empty: CreatureCard = CreatureCard("", 0, 0, SacrificeAttribute.Nil(), Set.empty, Common)

case class CreatureCard(
    name: String,
    attack: Int,
    health: Int,
    sacrificeAttribute: SacrificeAttribute,
    seals: Set[Seal],
    rarity: Rarity
) extends Card:
  override infix def named(name: String): CreatureCard = this.copy(name = name)

  override infix def withHealth(health: Int): CreatureCard =
    if health >= 0 then this.copy(health = health) else this

  infix def withAttack(attack: Int): CreatureCard =
    if attack >= 0 then this.copy(attack = attack) else this

  override infix def addSeal(seal: Seal): CreatureCard = this.copy(seals = seals + seal)

  override infix def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): CreatureCard =
    if sacrificeAttribute.isValid then this.copy(sacrificeAttribute = sacrificeAttribute) else this

object SupportCard:
  def empty: SupportCard = SupportCard("", 0, SacrificeAttribute.Nil(), Set.empty, Common)

case class SupportCard(
    name: String,
    health: Int,
    sacrificeAttribute: SacrificeAttribute,
    seals: Set[Seal],
    rarity: Rarity
) extends Card:
  override infix def named(name: String): SupportCard = this.copy(name = name)

  override infix def withHealth(health: Int): SupportCard =
    if health >= 0 then this.copy(health = health) else this

  override infix def addSeal(seal: Seal): SupportCard = this.copy(seals = seals + seal)

  override infix def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): SupportCard =
    if sacrificeAttribute.isValid then this.copy(sacrificeAttribute = sacrificeAttribute) else this
