package org.scryption.game.model

case class GameState(deck: Deck.Deck, isGameOver: Boolean)


object GameState:

  def getInitialGameState: GameState = GameState(deck = Deck.getStandardDeck, isGameOver = false)
  def getInitialGameState(deck: Deck.Deck) : GameState = GameState(deck = deck, isGameOver = false)