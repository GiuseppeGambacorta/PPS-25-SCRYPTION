package org.scryption.game.model.events

import org.scryption.{GUIChannelInterface, GUIMessages}
import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.{Card, CardLibrary, DrawDecks, GameState, PlayerHand, SacrificeAttribute}
import org.scryption.game.model.PlayerHand.PlayerHand
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.SacrificeAttribute.{Blood, Bones}
import org.scryption.game.model.managers.{CombatManager, MovementManager, SacrificeManager}
import org.scryption.game.model.managers.CombatManager.given
import org.scryption.game.model.GameState
import org.scryption.game.model.bot.{BotStrategy, RandomBotStrategy}

import scala.annotation.tailrec
import scala.util.Random

enum TurnState:
  case draw
  case playerTurn
  case playerFight
  case botTurn
  case botFight

case class FightState(
                       scalePoints: Int,
                       bones: Int,
                       deck: Deck,
                       playerHand: PlayerHand,
                       board: Board
                     )

private val PlayerWinningPoints = 6
private val BotWinningPoints = -6
private val NumberOfCardsAtTheStartOfTheFight = 4
private val PlayerWon = false
private val PlayerLost = true

def fight(gameState: GameState, ch: GUIChannelInterface): GameState =
  val (initialCards, remainingDeck) = gameState.deck.drawRandom(NumberOfCardsAtTheStartOfTheFight)
  val initialFightState = FightState(
    scalePoints = 0,
    bones = 0,
    deck = remainingDeck,
    playerHand = PlayerHand.fromList(initialCards),
    board = generateRandomStartingBoard()
  )

  GameState(gameState.deck, isGameOver = loop(TurnState.draw, initialFightState, ch))

@tailrec
private def loop(turnState: TurnState, fightState: FightState, ch: GUIChannelInterface): Boolean =
  ch.sendToGui(GUIMessages.FightState(fightState, turnState))

  turnState match
    case TurnState.draw =>
      if fightState.scalePoints <= BotWinningPoints then PlayerLost
      else
        val (nextTurn, nextState) = handleDrawPhase(fightState, ch)
        loop(nextTurn, nextState, ch)

    case TurnState.playerTurn =>
      val (nextTurn, nextState) = handlePlayerTurnPhase(fightState, ch)
      loop(nextTurn, nextState, ch)

    case TurnState.playerFight =>
      val (nextTurn, nextState) = handleFightPhase(fightState, ch, isPlayerAttacking = true)
      loop(nextTurn, nextState, ch)

    case TurnState.botTurn =>
      if fightState.scalePoints >= PlayerWinningPoints then PlayerWon
      else
        val bot: BotStrategy = RandomBotStrategy()
        val fightStateAfterBotPlays = bot.playTurn(fightState)
        loop(TurnState.botFight, fightStateAfterBotPlays, ch)

    case TurnState.botFight =>
      val (nextTurn, nextState) = handleFightPhase(fightState, ch, isPlayerAttacking = false)
      loop(nextTurn, nextState, ch)

// ============================================================================
// Phase Handlers (Restituiscono la tupla (TurnState, FightState))
// ============================================================================

private def handleDrawPhase(fightState: FightState, ch: GUIChannelInterface): (TurnState, FightState) =
  ch.receiveFromGui match
    case GUIMessages.DrawFromSquirrel =>
      val updatedHand = fightState.playerHand addCard CardLibrary.squirrel
      (TurnState.playerTurn, fightState.copy(playerHand = updatedHand))

    case GUIMessages.DrawFromDeck =>
      DrawDecks(fightState.deck).drawFromMain() match
        case Some((drawnCard, updatedDrawDecks)) =>
          val nextState = fightState.copy(
            deck = updatedDrawDecks.mainDeck,
            playerHand = fightState.playerHand addCard drawnCard
          )
          (TurnState.playerTurn, nextState)
        case None =>
          (TurnState.draw, fightState)

    case _ =>
      ch.clear()
      (TurnState.draw, fightState)

private def handlePlayerTurnPhase(fightState: FightState, ch: GUIChannelInterface): (TurnState, FightState) =
  ch.receiveFromGui match
    case GUIMessages.CardToPlay(card, position) =>
      val updatedState = playCardWithoutSacrifice(fightState, card, position)
      (TurnState.playerTurn, updatedState)

    case GUIMessages.CardToPlayWithSacrifices(card, position, sacrificesPositions) =>
      val updatedState = playCardWithSacrifices(fightState, card, position, sacrificesPositions)
      (TurnState.playerTurn, updatedState)

    // Esempio: aggiungi qui l'evento di fine turno se la GUI invia un messaggio EndTurn
    // case GUIMessages.EndTurn => (TurnState.playerFight, fightState)
    case GUIMessages.EndPlayerTurn => (TurnState.playerFight, fightState)


    case _ =>
      (TurnState.playerTurn, fightState)

private def handleFightPhase(
                              fightState: FightState,
                              ch: GUIChannelInterface,
                              isPlayerAttacking: Boolean
                            ): (TurnState, FightState) =
  val (attackerRowIdx, defenderRowIdx) =
    if isPlayerAttacking then (IndexOfPlayerRow, IndexOfBotRow)
    else (IndexOfBotRow, IndexOfPlayerRow)

  val attackerRow = fightState.board(attackerRowIdx)
  val defenderRow = fightState.board(defenderRowIdx)
  val result = CombatManager().executeRowAttack(attackerRow, defenderRow)

  val movedAttackerRow = MovementManager().resolveRowMovements(attackerRow)
  val finalBoard = fightState.board
    .updateRow(defenderRowIdx, result.updatedOpponentRow)
    .updateRow(attackerRowIdx, movedAttackerRow)

  val boardAfterQueue = if !isPlayerAttacking then
    MovementManager().resolveBotQueueMovement(finalBoard)
  else
    finalBoard

  val deltaPoints = if isPlayerAttacking then result.damageDelta else -result.damageDelta
  val nextTurn = if isPlayerAttacking then TurnState.botTurn else TurnState.draw

  val newState = fightState.copy(
    scalePoints = fightState.scalePoints + deltaPoints,
    board = boardAfterQueue,
    bones = fightState.bones + result.earnedBones,
    playerHand = fightState.playerHand.addCards(result.returnedToHandCards),
    deck = fightState.deck
  )

  (nextTurn, newState)

// ============================================================================
// Helper Functions for Card Placement Logic
// ============================================================================

private def playCardWithoutSacrifice(fightState: FightState, card: Card[?], position: Int): FightState =
  fightState.board(IndexOfPlayerRow)(position) match
    case Some(_) => fightState // Slot occupato
    case None =>
      card.sacrificeAttribute match
        case SacrificeAttribute.Nil =>
          val newBoard = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))
          val boardWithGuardian = newBoard.updateRow(IndexOfBotRow, MovementManager().resolveGuardianMovement(newBoard(IndexOfBotRow), position))
          fightState.copy(board = boardWithGuardian, playerHand = fightState.playerHand.removeCard(card))

        case SacrificeAttribute.Bones(amount) if fightState.bones >= amount =>
          val newBoard = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))
          fightState.copy(board = newBoard, bones = fightState.bones - amount, playerHand = fightState.playerHand.removeCard(card))

        case _ => fightState // Ossa insufficienti o tipo di sacrificio errato

private def playCardWithSacrifices(
                                    fightState: FightState,
                                    card: Card[?],
                                    position: Int,
                                    sacrificesPositions: List[(Int, Int)]
                                  ): FightState =
  card.sacrificeAttribute match
    case Blood(amount) =>
      val currentSlot = fightState.board(IndexOfPlayerRow)(position)
      val isValidTarget = currentSlot.isEmpty || sacrificesPositions.contains((IndexOfPlayerRow, position))

      if isValidTarget then
        val sacrificeResult = SacrificeManager().resolveSacrifices(fightState.board, sacrificesPositions)
        if sacrificeResult.generatedBlood >= amount then
          val newBoard = sacrificeResult.updatedBoard.updatedSlot((IndexOfPlayerRow, position), Some(card))
          val boardWithGuardian = newBoard.updateRow(IndexOfBotRow, MovementManager().resolveGuardianMovement(newBoard(IndexOfBotRow), position))
          fightState.copy(board = boardWithGuardian, bones = fightState.bones + sacrificeResult.generatedBones, playerHand = fightState.playerHand.removeCard(card))
        else fightState
      else fightState

    case _ => fightState

private def generateRandomStartingBoard(): Board =
  val random = new Random()
  val possibleBotCards = CardLibrary.getADeckWithAllTheLibrary.toList
  val numBotCards = random.nextInt(2) + 1
  val botTargetCols = random.shuffle((0 until ColsCount).toList).take(numBotCards)

  val boardWithEnemies = botTargetCols.foldLeft(Board.empty): (board, col) =>
    val randomCard = possibleBotCards(random.nextInt(possibleBotCards.length))
    board.updatedSlot((IndexOfBotRow, col), Some(randomCard))

  val obstacles = List(CardLibrary.boulder, CardLibrary.stump, CardLibrary.grandFir)
  val playerTargetCols = (0 until ColsCount).filter(_ => random.nextDouble() < 0.25)
  playerTargetCols.foldLeft(boardWithEnemies): (board, col) =>
    val randomObstacle = obstacles(random.nextInt(obstacles.length))
    board.updatedSlot((IndexOfPlayerRow, col), Some(randomObstacle))
