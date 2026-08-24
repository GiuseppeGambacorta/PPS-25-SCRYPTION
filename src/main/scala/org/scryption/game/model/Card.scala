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
    * @param name
    *   The new name of the card.
    * @return
    *   a new card instance of type C.
    */
  infix def named(name: String): C =
    copyCard(name = name)

  /** Creates a new instance of the card with updated health. If the provided health is negative, returns the current
    * instance unchanged.
    *
    * @param health
    *   The new health value (must be >= 0).
    * @return
    *   a new card instance of type C, or the same instance if invalid.
    */
  infix def withHealth(health: Int): C =
    if health >= 0 then copyCard(health = health) else this.asInstanceOf[C]

  /** Creates a new instance of the card with an additional seal.
    *
    * @param seal
    *   The seal to add.
    * @return
    *   a new card instance of type C.
    */
  infix def addSeal(seal: Seal): C = copyCard(seals = this.seals + seal)

  /** Creates a new instance of the card without the specified seal.
    *
    * @param seal
    *   The seal to remove.
    * @return
    *   a new card instance of type C.
    */
  infix def removeSeal(seal: Seal): C = copyCard(seals = this.seals - seal)

  /** Creates a new instance of the card with an updated sacrifice cost.
    *
    * @param sacrificeAttribute
    *   The new cost (Blood, Bones, or Nil).
    * @return
    *   a new card instance of type C, or the same instance if invalid.
    */
  infix def withSacrificeAttribute(sacrificeAttribute: SacrificeAttribute): C =
    if sacrificeAttribute.isValid then copyCard(sacrificeAttribute = sacrificeAttribute) else this.asInstanceOf[C]

  /** Creates a new instance of the card with the specified rarity.
    *
    * @param rarity
    *   The rarity to change the card to.
    * @return
    *   a new card instance of type C
    */
  infix def withRarity(rarity: Rarity): C =
    copyCard(rarity = rarity)

  /** Protected template method used to internally clone the card. Must be implemented by concrete classes to bridge
    * with their native `copy` method.
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
  case Nil

  def isValid: Boolean = this match
    case Blood(v) => v >= 0
    case Bones(v) => v >= 0
    case Nil      => true

enum Direction:
  case Left
  case Right

enum Seal:
  case RabbitHole // When this card is played, a Rabbit is created in your hand.
  case BeesWithin // When this card is struck, a Bee is created in your hand.
  case Sprinter(direction: Direction) // At the end of the owner's turn, this card moves in the sigil's direction.
  case TouchOfDeath // This card instantly kills any card it damages.
  case Fledgling // After surviving for 1 turn, this card grows into a stronger form.
  case DamBuilder // When this card is played, Dams are created on adjacent empty spaces.
  case Burrower // This card will move to any empty space that is attacked by an enemy to block it.
  case Fecundity // When this card is played, a copy of it enters your hand.
  case BoneKing // When this card dies, 4 Bones are awarded instead of 1.
  case Waterborne // On the opponent's turn, creatures attacking this card's space attack directly.
  case Unkillable // When this card perishes, a copy of it enters your hand.
  case SharpQuills // Once this card is struck, the striker is dealt 1 damage.
  case Hefty // At the end of the owner's turn, this and adjacent cards move in the sigil's direction.
  case AntSpawner // When this card is played, an Ant enters your hand.
  case Guardian // When an opposing card is played opposite an empty space, this card moves to that space.
  case Airborne // This card will ignore opposing cards and strike an opponent directly.
  case ManyLives // When this card is sacrificed, it does not perish.
  case WorthySacrifice // This card counts as 3 Blood rather than 1 Blood when sacrificed.
  case MightyLeap // This card blocks opposing Airborne creatures.
  case BifurcatedStrike // This card will strike each opposing space to the left and right of the spaces across it.
  case TrifurcatedStrike // This card will deal damage to the opposing spaces left, right, and opposite of it.
  case FrozenAway // When this card perishes, the creature inside takes its place.
  case TrinketBearer // When this card is played, you will receive an item if you have room.
  case Leader // Creatures adjacent to this card gain 1 Power.
  case Stinky // The creature opposing this card loses 1 Power.

enum Rarity:
  case Common
  case Rare

object CreatureCard:
  /** @return
    *   a new empty CreatureCard with a randomly generated UUID.
    */
  def empty: CreatureCard = CreatureCard(UUID.randomUUID(), "", 0, 0, SacrificeAttribute.Nil, Set.empty, Common)

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
    * @param attack
    *   The new attack value (must be >= 0).
    * @return
    *   A new CreatureCard instance, or the same instance if invalid.
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
    this.copy(
      id = id,
      name = name,
      health = health,
      sacrificeAttribute = sacrificeAttribute,
      seals = seals,
      rarity = rarity
    )

object SupportCard:
  /** @return
    *   a new empty SupportCard with a randomly generated UUID.
    */
  def empty: SupportCard = SupportCard(UUID.randomUUID(), "", 0, SacrificeAttribute.Nil, Set.empty, Common)

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
    this.copy(
      id = id,
      name = name,
      health = health,
      sacrificeAttribute = sacrificeAttribute,
      seals = seals,
      rarity = rarity
    )
