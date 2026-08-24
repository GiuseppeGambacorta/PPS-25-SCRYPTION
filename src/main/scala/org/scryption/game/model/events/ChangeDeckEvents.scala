package org.scryption.game.model.events

import org.scryption.EventMessages
import org.scryption.GameMessagesChannel
import org.scryption.game.model.*
import org.scryption.game.model.Deck
import org.scryption.Trial

import scala.annotation.tailrec

type Event = (GameState, GameMessagesChannel) => GameState

// Maximum number of cards sent to the GUI for selection
val cardsNumberForGui = 5

/**
 * Event: Draws 3 random cards from the library and sends them to the GUI.
 * The player picks one card to add to their deck.
 */
def getANewCardEvent(gameState: GameState, ch: GameMessagesChannel): GameState = {
  selectCardEvent(gameState, ch, EventMessages.Cards(CardLibrary.getDeckWithAverageCards.drawRandom(3)._1))
}

/**
 * Event: Draws 3 random rare cards from the library and sends them to the GUI.
 * The player picks one card to add to their deck.
 */
private def getANewRareCardEvent(gameState: GameState, ch: GameMessagesChannel): GameState = {
  selectCardEvent(gameState, ch, EventMessages.Cards(CardLibrary.getADeckWithRareCards.drawRandom(3)._1))
}

/**
 * Event: Receives 3 cards and sends them to the GUI.
 * The player picks one card to add to their deck.
 */
@tailrec
private def selectCardEvent(gameState: GameState, ch: GameMessagesChannel, cards: EventMessages.Cards): GameState = {
  ch.sendToGui(cards)
  val message = ch.receiveFromGui

  message match {
    case EventMessages.SingleCard(card) =>
      ch.sendToGui(EventMessages.End)
      gameState.copy(deck = gameState.deck.addCard(card))
    case _ =>
      ch.clear()
      selectCardEvent(gameState, ch, cards)
  }
}

/**
 * Event Mushroom Expert (Mycologists):
 * Checks for duplicate cards in the deck. If present, allows the GUI to pick one:
 * removes 2 copies of that card from the deck and adds 1 fused/boosted version (stats doubled).
 * If there are no duplicates in the deck, returns the unchanged GameState immediately.
 */
@tailrec
def mushRoomsExpertEvent(gameState: GameState, ch: GameMessagesChannel): GameState = {
  val deckList = gameState.deck.toList

  // Group cards to identify those occurring at least twice in the deck
  val duplicateCards = deckList
    .groupBy(identity)
    .filter { case (_, instances) => instances.size >= 2 }
    .keys
    .toList

  if duplicateCards.nonEmpty then
    ch.sendToGui(EventMessages.Cards(duplicateCards.take(cardsNumberForGui)))
  else {
    return gameState
  }

  ch.receiveFromGui match {
    case EventMessages.SingleCard(selectedCard) =>
      val upgradedCard = modifyCreature(selectedCard)(c =>
        c withAttack (c.attack * 2) withHealth (c.health * 2)
      )

      // Remove two exact copies of the selected card
      val updatedDeck = gameState.deck
        .removeCard(selectedCard)
        .removeCard(selectedCard)
        .addCard(upgradedCard)

      ch.sendToGui(EventMessages.End)
      gameState.copy(deck = updatedDeck)

    case _ =>
      ch.clear()
      mushRoomsExpertEvent(gameState, ch)
  }
}

/**
 * Event Firecamp (Attack):
 * Allows selecting a card from the deck to increase its attack stat by +1.
 */
def fireCampEvent_Attack(gameState: GameState, ch: GameMessagesChannel): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withAttack (c.attack + 1))
  )

/**
 * Event Firecamp (Health):
 * Allows selecting a card from the deck to increase its health stat by +2.
 */
def fireCampEvent_Health(gameState: GameState, ch: GameMessagesChannel): GameState =
  substituteACard(
    gameState,
    ch,
    card => modifyCreature(card)(c => c withHealth (c.health + 2))
  )

/**
 * Event Sacrifice:
 * Allows sacrificing a card with seals to transfer all its seals onto another card in the deck.
 * If no cards in the deck have any seals, the event terminates and returns the original GameState.
 */
@tailrec
def sacrificeEvent(gameState: GameState, ch: GameMessagesChannel): GameState = {
  val cardsWithSeals = gameState.deck.toList.filter(c => c.seals.nonEmpty)

  if cardsWithSeals.nonEmpty then
    ch.sendToGui(
      EventMessages.Cards(
        cardsWithSeals.take(cardsNumberForGui)
      )
    )
  else
    return gameState

  ch.receiveFromGui match {
    case EventMessages.SingleCard(cardToRemove) =>
      val deckWithoutTheCard = gameState.deck.removeCard(cardToRemove)

      ch.sendToGui(
        EventMessages.Cards(
          deckWithoutTheCard.drawRandom(Math.min(cardsNumberForGui, deckWithoutTheCard.size), 42)._1
        )
      )

      ch.receiveFromGui match {
        case EventMessages.SingleCard(cardToUpgrade) =>
          val seals = cardToRemove.seals
          val updatedCard = seals.foldLeft(cardToUpgrade)((card, seal) => card.addSeal(seal))

          val updatedDeck = gameState.deck
            .removeCard(cardToRemove)
            .removeCard(cardToUpgrade)
            .addCard(updatedCard)

          ch.sendToGui(EventMessages.End)
          gameState.copy(deck = updatedDeck)

        case _ =>
          ch.clear()
          sacrificeEvent(gameState, ch)
      }

    case _ =>
      ch.clear()
      sacrificeEvent(gameState, ch)
  }
}


/**
 * Event Trial:
 * Invia alla GUI fino a un massimo di 10 carte del mazzo.
 * In base alla scelta ricevuta (TrialChoice), calcola la somma dell'attributo selezionato
 * e verifica il superamento della relativa soglia.
 * Se la prova è superata, viene proposta una carta casuale dalla libreria: una volta
 * confermata dalla GUI, viene aggiunta al mazzo. Altrimenti, l'evento termina.
 */
@tailrec
def trialEvent(gameState: GameState, ch: GameMessagesChannel): GameState = {
  val healthThreshold = 10
  val attackThreshold = 6
  val sealsThreshold = 4

  val cardsForTrial = gameState.deck.toList.take(10)
  ch.sendToGui(EventMessages.Cards(cardsForTrial))

  ch.receiveFromGui match {
    case EventMessages.TrialChoice(choice) =>
      val totalValue = choice match {
        case Trial.Health =>
          cardsForTrial.map(_.health).sum

        case Trial.Attack =>
          cardsForTrial.map {
            case c: CreatureCard => c.attack
            case _               => 0
          }.sum

        case Trial.Seals =>
          cardsForTrial.map(_.seals.size).sum
      }

      val isSuccess = choice match {
        case Trial.Health => totalValue >= healthThreshold
        case Trial.Attack => totalValue >= attackThreshold
        case Trial.Seals  => totalValue >= sealsThreshold
      }

      if isSuccess then
        val (rewardList, _) = CardLibrary.getADeckWithRareCards.drawRandom(1)
        rewardList.headOption match {
          case Some(rewardCard) =>
            ch.sendToGui(EventMessages.SingleCard(rewardCard))
            handleTrialReward(gameState, ch, rewardCard)
          case None =>
            ch.sendToGui(EventMessages.End)
            gameState
        }
      else
        ch.sendToGui(EventMessages.End)
        gameState

    case _ =>
      ch.clear()
      trialEvent(gameState, ch)
  }
}

/**
 * Helper ricorsivo in attesa della conferma da parte della GUI della carta ricompensa ricevuta.
 */
@tailrec
private def handleTrialReward(gameState: GameState, ch: GameMessagesChannel, rewardCard: Card[?]): GameState = {
  ch.receiveFromGui match {
    case EventMessages.SingleCard(card) if card == rewardCard =>
      ch.sendToGui(EventMessages.End)
      gameState.copy(deck = gameState.deck.addCard(card))
    case _ =>
      ch.clear()
      ch.sendToGui(EventMessages.SingleCard(rewardCard))
      handleTrialReward(gameState, ch, rewardCard)
  }
}

/**
 * Generic helper function for events that replace a card in the deck
 * with a modified version produced by transformation `f`.
 */
@tailrec
private def substituteACard(gameState: GameState, ch: GameMessagesChannel, f: Card[?] => Card[?]): GameState = {
  ch.sendToGui(
    EventMessages.Cards(gameState.deck.drawRandom(Math.min(cardsNumberForGui, gameState.deck.size), 42)._1)
  )
  val message = ch.receiveFromGui

  message match {
    case EventMessages.SingleCard(card) =>
      ch.sendToGui(EventMessages.End)
      val updatedDeck = gameState.deck removeCard card addCard f(card)
      gameState.copy(deck = updatedDeck)
    case _ =>
      ch.clear()
      substituteACard(gameState, ch, f)
  }
}

/**
 * Helper to modify card attributes only if the card is a CreatureCard.
 */
private def modifyCreature(card: Card[?])(f: CreatureCard => Card[?]): Card[?] = card match {
  case c: CreatureCard => f(c)
  case other           => other
}
