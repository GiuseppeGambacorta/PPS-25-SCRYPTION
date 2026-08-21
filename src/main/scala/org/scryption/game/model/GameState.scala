package org.scryption.game.model

import org.scryption.game.model.items.GameItem

case class GameState(deck: Deck.Deck, inventory: List[GameItem], isGameOver: Boolean)


object GameState:

  def getInitialGameState: GameState = GameState(deck = Deck.getStandardDeck, List.empty, isGameOver = false)
  def getInitialGameState(deck: Deck.Deck) : GameState = GameState(deck = deck, List.empty, isGameOver = false)