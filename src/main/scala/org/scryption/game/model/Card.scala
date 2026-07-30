package org.scryption.game.model

import org.scryption.game.model.Rarity.Common

import java.util.UUID

sealed trait Card[C <: Card[C]]:
  def id: UUID
  def name: String
  def health: Int
  def sacrificeAttribute: SacrificeAttribute
  def seals: Set[Seal]
  def rarity: Rarity

  infix def named(name: String): C =
    copyCard(name = name)

  infix def withHealth(health: Int): C =
    if health >= 0 then copyCard(health = health) else this.asInstanceOf[C]

  infix def addSeal(seal: Seal): C = copyCard(seals = this.seals + seal)

  infix def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): C =
    if sacrificeAttribute.isValid then copyCard(sacrificeAttribute = sacrificeAttribute) else this.asInstanceOf[C]

  protected def copyCard(
      id: UUID = this.id,
      name: String = this.name,
      health: Int = this.health,
      sacrificeAttribute: SacrificeAttribute = this.sacrificeAttribute,
      seals: Set[Seal] = this.seals,
      rarity: Rarity = this.rarity
  ): C

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
  def empty: CreatureCard = CreatureCard(UUID.randomUUID(), "", 0, 0, SacrificeAttribute.Nil(), Set.empty, Common)

case class CreatureCard(
    id: UUID,
    name: String,
    attack: Int,
    health: Int,
    sacrificeAttribute: SacrificeAttribute,
    seals: Set[Seal],
    rarity: Rarity
) extends Card[CreatureCard]:

  infix def withAttack(attack: Int): CreatureCard =
    if attack >= 0 then this.copy(attack = attack) else this

  override protected def copyCard(
      id: UUID = this.id,
      name: String = this.name,
      health: Int = this.health,
      sacrificeAttribute: SacrificeAttribute = this.sacrificeAttribute,
      seals: Set[Seal] = this.seals,
      rarity: Rarity = this.rarity
  ): CreatureCard =
    this.copy(id = id, name = name, health = health, sacrificeAttribute = sacrificeAttribute, seals = seals, rarity = rarity)


object SupportCard:
  def empty: SupportCard = SupportCard(UUID.randomUUID(), "", 0, SacrificeAttribute.Nil(), Set.empty, Common)

case class SupportCard(
    id: UUID,
    name: String,
    health: Int,
    sacrificeAttribute: SacrificeAttribute,
    seals: Set[Seal],
    rarity: Rarity
) extends Card[SupportCard]:

  override protected def copyCard(
      id: UUID = this.id,
      name: String = this.name,
      health: Int = this.health,
      sacrificeAttribute: SacrificeAttribute = this.sacrificeAttribute,
      seals: Set[Seal] = this.seals,
      rarity: Rarity = this.rarity
  ): SupportCard =
    this.copy(id = id, name = name, health = health, sacrificeAttribute = sacrificeAttribute, seals = seals, rarity = rarity)