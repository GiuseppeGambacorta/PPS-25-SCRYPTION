package org.scryption.view

import org.scryption.game.model.*

// Placeholder class to represent Cards

final case class CardViewInfo(
    name: String,
    cost: String,
    attack: String,
    health: String,
    sigils: List[String] = Nil,
    cardType: String = ""
)

//  Converts a game-model Card into the view-facing CardViewInfo

extension (card: Card[?])
  def toViewInfo: CardViewInfo =
    CardViewInfo(
      name = card.name,
      cost = CardViewConversions.costLabel(card.sacrificeAttribute),
      attack = CardViewConversions.attackLabel(card),
      health = card.health.toString,
      sigils = card.seals.toList.map(CardViewConversions.sealLabel),
      cardType = CardViewConversions.rarityLabel(card.rarity)
    )

private object CardViewConversions:

  def attackLabel(card: Card[?]): String = card match
    case c: CreatureCard => c.attack.toString
    case _: SupportCard  => "" // no attack stat to show

  def costLabel(attribute: SacrificeAttribute): String = attribute match
    case SacrificeAttribute.Blood(value) => s"${value}blood"
    case SacrificeAttribute.Bones(value) => s"${value}bone"
    case SacrificeAttribute.Nil()        => ""

  def sealLabel(seal: Seal): String = seal match
    case Seal.Airborne          => "airborne"
    case Seal.Wall              => "mighty_leap"
    case Seal.BifurcatedStrike  => "bifurcated_strike"
    case Seal.TrifurcatedStrike => "trifurcated_strike"
    case Seal.TouchOfDeath      => "touch_of_death"
    case Seal.Guardian          => "guardian"
    case Seal.Sprinter          => "sprinter"
    case Seal.Immortal          => "unkillable"
    case Seal.BoneKing          => "bone_king"
    case Seal.InfiniteSacrifice => "many_lives"

  def rarityLabel(rarity: Rarity): String = rarity match
    case Rarity.Common => ""
    case Rarity.Rare   => "rare"
