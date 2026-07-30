package org.scryption.view

// Placeholder class to represent Cards

final case class CardViewInfo(
    name: String,
    cost: String,
    attack: String,
    health: String,
    sigils: List[String] = Nil,
    cardType: String = ""
)
