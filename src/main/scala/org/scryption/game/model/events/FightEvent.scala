package org.scryption.game.model.events

import org.scryption.FightMessages
import org.scryption.GameMessagesChannel
import org.scryption.game.model.Card
import org.scryption.game.model.CardLibrary
import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.DrawDecks
import org.scryption.game.model.GameState
import org.scryption.game.model.PlayerHand
import org.scryption.game.model.PlayerHand.PlayerHand
import org.scryption.game.model.SacrificeAttribute
import org.scryption.game.model.SacrificeAttribute.Blood
import org.scryption.game.model.SacrificeAttribute.Bones
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.bot.BotStrategy
import org.scryption.game.model.bot.RandomBotStrategy
import org.scryption.game.model.items.*
import org.scryption.game.model.managers.CombatManager
import org.scryption.game.model.managers.CombatManager.given
import org.scryption.game.model.managers.MovementManager
import org.scryption.game.model.managers.SacrificeManager

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
                       board: Board,
                       inventory: List[GameItem]
                     )

private val PlayerWinningPoints = 6
private val BotWinningPoints = -6
private val NumberOfCardsAtTheStartOfTheFight = 4
private val PlayerWon = false
private val PlayerLost = true

/** Entry point of the combat sub-loop. Draws the starting hand, builds the initial
 * [[FightState]] and runs the turn-state machine until victory or defeat, returning
 * the resulting [[GameState]].
 */
def fightEvent(gameState: GameState, ch: GameMessagesChannel): GameState =
  val (initialCards, remainingDeck) = gameState.deck.drawRandom(NumberOfCardsAtTheStartOfTheFight)
  val initialFightState = FightState(
    scalePoints = 0,
    bones = 0,
    deck = remainingDeck,
    playerHand = PlayerHand.fromList(initialCards),
    board = generateRandomStartingBoard(),
    inventory = gameState.inventory
  )

  GameState(gameState.deck, gameState.inventory, isGameOver = loop(TurnState.draw, initialFightState, ch))

/** Tail-recursive turn-state machine driving the combat. Notifies the view of the current
 * state, dispatches to the appropriate phase handler based on `turnState`, and recurses
 * until a winning or losing threshold on `scalePoints` is reached.
 */
@tailrec
private def loop(turnState: TurnState, fightState: FightState, ch: GameMessagesChannel): Boolean =
  ch.sendToGui(FightMessages.State(fightState, turnState))

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
      val (nextTurn, nextState) = handleFightPhase(fightState, isPlayerAttacking = true)
      loop(nextTurn, nextState, ch)

    case TurnState.botTurn =>
      if fightState.scalePoints >= PlayerWinningPoints then PlayerWon
      else
        val bot: BotStrategy = RandomBotStrategy()
        val fightStateAfterBotPlays = bot.playTurn(fightState)
        loop(TurnState.botFight, fightStateAfterBotPlays, ch)

    case TurnState.botFight =>
      val (nextTurn, nextState) = handleFightPhase(fightState, isPlayerAttacking = false)
      loop(nextTurn, nextState, ch)

// ============================================================================
// Phase Handlers (Restituiscono la tupla (TurnState, FightState))
// ============================================================================

/** Handles the draw phase: waits for the player to draw from the squirrel deck or from the
 * main deck, updates the hand accordingly, and moves the turn state forward.
 */
private def handleDrawPhase(fightState: FightState, ch: GameMessagesChannel): (TurnState, FightState) =
  ch.receiveFromGui match
    case FightMessages.DrawFromSquirrel =>
      val updatedHand = fightState.playerHand addCard CardLibrary.squirrel
      (TurnState.playerTurn, fightState.copy(playerHand = updatedHand))

    case FightMessages.DrawFromDeck =>
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

/** Handles the player's turn: dispatches card placement (with or without sacrifices), item
 * usage, and the transition to the fight phase once the player ends their turn.
 */
private def handlePlayerTurnPhase(fightState: FightState, ch: GameMessagesChannel): (TurnState, FightState) =
  ch.receiveFromGui match
    case FightMessages.CardToPlay(card, position) =>
      val updatedState = playCardWithoutSacrifice(fightState, card, position)
      (TurnState.playerTurn, updatedState)

    case FightMessages.CardToPlayWithSacrifices(card, position, sacrificesPositions) =>
      val updatedState = playCardWithSacrifices(fightState, card, position, sacrificesPositions)
      (TurnState.playerTurn, updatedState)

    case FightMessages.UseItem(item, target) =>
      if fightState.inventory.contains(item) then
        val updatedState = item.use(fightState, target)
        (TurnState.playerTurn, updatedState)
      else (TurnState.playerTurn, fightState)

    case FightMessages.EndPlayerTurn =>
      (TurnState.playerFight, fightState)

    case _ =>
      (TurnState.playerTurn, fightState)

/** Resolves a full attack phase for either the player or the bot: computes the combat result
 * between attacker and defender rows, applies movement rules, updates the damage scale
 * (`scalePoints`), and determines the next turn state.
 */
private def handleFightPhase(
                              fightState: FightState,
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

  val boardAfterQueue =
    if !isPlayerAttacking then MovementManager().resolveBotQueueMovement(finalBoard)
    else finalBoard

  val deltaPoints = if isPlayerAttacking then result.damageDelta else -result.damageDelta
  val nextTurn = if isPlayerAttacking then TurnState.botTurn else TurnState.draw

  val newState = fightState.copy(
    scalePoints = fightState.scalePoints + deltaPoints,
    board = boardAfterQueue,
    bones = fightState.bones + result.earnedBones,
    playerHand = fightState.playerHand.addCards(result.returnedToHandCards),
    deck = fightState.deck,
    inventory = fightState.inventory
  )

  (nextTurn, newState)

// ============================================================================
// Helper Functions for Card Placement Logic
// ============================================================================

/** Plays a card without sacrifices at the given position, if the slot is free and the card's
 * sacrifice requirement (none, or a satisfiable bone cost) is met; otherwise returns the
 * state unchanged.
 */
private def playCardWithoutSacrifice(fightState: FightState, card: Card[?], position: Int): FightState =
  fightState.board(IndexOfPlayerRow)(position) match
    case Some(_) => fightState // Slot occupato
    case None =>
      card.sacrificeAttribute match
        case SacrificeAttribute.Nil =>
          val newBoard = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))
          val boardWithGuardian = newBoard.updateRow(
            IndexOfBotRow,
            MovementManager().resolveGuardianMovement(newBoard(IndexOfBotRow), position)
          )
          fightState.copy(board = boardWithGuardian, playerHand = fightState.playerHand.removeCard(card))

        case SacrificeAttribute.Bones(amount) if fightState.bones >= amount =>
          val newBoard = fightState.board.updatedSlot((IndexOfPlayerRow, position), Some(card))
          fightState.copy(
            board = newBoard,
            bones = fightState.bones - amount,
            playerHand = fightState.playerHand.removeCard(card)
          )

        case _ => fightState // Ossa insufficienti o tipo di sacrificio errato

/** Plays a card that requires a blood sacrifice, resolving the sacrifice of the given board
 * positions first; the card is placed only if the target slot is valid and the resulting
 * blood meets the card's requirement, otherwise the state is returned unchanged.
 */
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
          val boardWithGuardian = newBoard.updateRow(
            IndexOfBotRow,
            MovementManager().resolveGuardianMovement(newBoard(IndexOfBotRow), position)
          )
          fightState.copy(
            board = boardWithGuardian,
            bones = fightState.bones + sacrificeResult.generatedBones,
            playerHand = fightState.playerHand.removeCard(card)
          )
        else fightState
      else fightState

    case _ => fightState

/** Generates a random starting board for a fight, placing one or two random enemy creatures
 * on the bot row and, with a small independent probability per column, a random obstacle
 * on the player row.
 */
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