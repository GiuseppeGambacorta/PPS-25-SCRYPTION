package org.scryption.view

import org.scryption.game.model.*

final case class CardViewInfo(
                               name: String,
                               cost: String,
                               attack: String,
                               health: String,
                               defaultSigils: List[String] = Nil,
                               addedSigils: List[String] = Nil,
                               cardType: String = ""
                             )

//  Converts a game-model Card into the view-facing CardViewInfo

extension (card: Card[?])
  def toViewInfo: CardViewInfo =
    val (defaultSeals, addedSeals) = CardViewConversions.splitSeals(card)

    CardViewInfo(
      name = card.name,
      cost = CardViewConversions.costLabel(card.sacrificeAttribute),
      attack = CardViewConversions.attackLabel(card),
      health = card.health.toString,
      defaultSigils = defaultSeals.toList.map(CardViewConversions.sealLabel),
      addedSigils = addedSeals.toList.map(CardViewConversions.sealLabel),
      cardType = CardViewConversions.rarityLabel(card.rarity)
    )

private object CardViewConversions:

  def attackLabel(card: Card[?]): String = card match
    case c: CreatureCard => c.attack.toString
    case _: SupportCard  => ""

  def costLabel(attribute: SacrificeAttribute): String = attribute match
    case SacrificeAttribute.Blood(value) => s"${value}blood"
    case SacrificeAttribute.Bones(value) => s"${value}bone"
    case _        => ""
  
  def splitSeals(card: Card[?]): (Set[Seal], Set[Seal]) =
    val templateSeals: Set[Seal] =
      CardLibrary.byName(card.name).map(_.seals).getOrElse(Set.empty)

    val defaultSeals = card.seals.intersect(templateSeals)
    val addedSeals = card.seals.diff(templateSeals)

    (defaultSeals, addedSeals)

  def sealLabel(seal: Seal): String = seal match
    case Seal.RabbitHole                => "rabbit_hole"
    case Seal.BeesWithin                => "bees_within"
    case Seal.Sprinter(Direction.Right) => "sprinted"
    case Seal.Sprinter(Direction.Left)  => "sprinted"
    case Seal.TouchOfDeath              => "touch_of_death"
    case Seal.Fledgling                 => "fledgling"
    case Seal.DamBuilder                => "dam_builder"
    case Seal.Burrower                  => "burrower"
    case Seal.Fecundity                 => "fecundity"
    case Seal.BoneKing                  => "bone_king"
    case Seal.Waterborne                => "waterborne"
    case Seal.Unkillable                => "unkillable"
    case Seal.SharpQuills               => "sharp_quills"
    case Seal.Hefty                     => "hefty"
    case Seal.AntSpawner                => "ant_spawner"
    case Seal.Guardian                  => "guardian"
    case Seal.Airborne                  => "airborne"
    case Seal.ManyLives                 => "many_lives"
    case Seal.WorthySacrifice           => "worthy_sacrifice"
    case Seal.MightyLeap                => "mighty_leap"
    case Seal.BifurcatedStrike          => "bifurcated_strike"
    case Seal.TrifurcatedStrike         => "trifurcated_strike"
    case Seal.FrozenAway                => "frozen_away"
    case Seal.TrinketBearer             => "trinket_bearer"
    case Seal.Leader                    => "leader"
    case Seal.Stinky                    => "stinky"

  def rarityLabel(rarity: Rarity): String = rarity match
    case Rarity.Common => ""
    case Rarity.Rare   => "rare"