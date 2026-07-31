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

  /** Creates a new instance of the card with an updated name.
   *
   * @param name The new name of the card.
   * @return a new card instance of type C.
   */
  infix def named(name: String): C =
    copyCard(name = name)

  /** Creates a new instance of the card with updated health.
   * If the provided health is negative, returns the current instance unchanged.
   *
   * @param health The new health value (must be >= 0).
   * @return a new card instance of type C, or the same instance if invalid.
   */
  infix def withHealth(health: Int): C =
    if health >= 0 then copyCard(health = health) else this.asInstanceOf[C]

  /** Creates a new instance of the card with an additional seal.
   *
   * @param seal The seal to add.
   * @return a new card instance of type C.
   */
  infix def addSeal(seal: Seal): C = copyCard(seals = this.seals + seal)

  /** Creates a new instance of the card with an updated sacrifice cost.
   *
   * @param sacrificeAttribute The new cost (Blood, Bones, or Nil).
   * @return a new card instance of type C, or the same instance if invalid.
   */
  infix def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): C =
    if sacrificeAttribute.isValid then copyCard(sacrificeAttribute = sacrificeAttribute) else this.asInstanceOf[C]

  /** Protected template method used to internally clone the card.
   * Must be implemented by concrete classes to bridge with their native `copy` method.
   */
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
  /** @return a new empty CreatureCard with a randomly generated UUID.
   */
  def empty: CreatureCard = CreatureCard(UUID.randomUUID(), "", 0, 0, SacrificeAttribute.Nil(), Set.empty, Common)

/** Represents a standard creature card that can attack.
 */
case class CreatureCard(
    id: UUID,
    name: String,
    attack: Int,
    health: Int,
    sacrificeAttribute: SacrificeAttribute,
    seals: Set[Seal],
    rarity: Rarity
) extends Card[CreatureCard]:

  /** Creates a new instance of the creature with updated attack.
   *
   * @param attack The new attack value (must be >= 0).
   * @return A new CreatureCard instance, or the same instance if invalid.
   */
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
  /** @return a new empty SupportCard with a randomly generated UUID.
   */
  def empty: SupportCard = SupportCard(UUID.randomUUID(), "", 0, SacrificeAttribute.Nil(), Set.empty, Common)

/** Represents a support card that cannot attack.
 */
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