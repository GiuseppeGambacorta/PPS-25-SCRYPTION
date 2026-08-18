package org.scryption.view.fight

import org.scryption.game.model.{Card, SacrificeAttribute}
import org.scryption.game.model.boardModel.Board
import org.scryption.game.model.events.{FightState, TurnState}
import org.scryption.{FightMessages, GUIChannelInterface}
import org.scryption.view.ViewModelFight

import scala.swing.*
import java.awt.{Color, Dimension}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FightView(viewModel : ViewModelFight) extends BorderPanel:

  var selectedCard: Option[Card[?]] = None
  var selectedSacrifices: List[(Int, Int)] = List.empty
  var currentBoard: Option[Board] = None
  
  viewModel.listenForUpdatedState(updateViews)

  private def onCardSelectedFromHand(cardOpt: Option[Card[?]]): Unit =
    selectedCard = cardOpt
    selectedSacrifices = List.empty
    boardView.updateSacrificeHighlights(selectedSacrifices)

  def onSlotClicked(row: Int, col: Int): Unit =
    if row == 2 then
      selectedCard.foreach: card =>
        val isSlotEmpty = currentBoard.exists(b => b(row)(col).isEmpty)

        card.sacrificeAttribute match
          case SacrificeAttribute.Blood(amount) =>
            val hasEnoughBlood = selectedSacrifices.length >= amount
            /* alternative approach:
            val currentBlood = currentBoard.map { board =>
              SacrificeManager.resolveSacrifices(board, selectedSacrifices).generatedBlood
            }.getOrElse(0)
             */
            val isTargetingSacrifice = selectedSacrifices.contains((row, col))
            if hasEnoughBlood && (isSlotEmpty || isTargetingSacrifice) then
              viewModel.cardToPlayWithSacrifices(card, col, selectedSacrifices)
              resetSelection()
            else if !isSlotEmpty then
              if isTargetingSacrifice then
                selectedSacrifices = selectedSacrifices.filterNot(_ == (row, col))
                boardView.updateSacrificeHighlights(selectedSacrifices)
              else if !hasEnoughBlood then
                selectedSacrifices = selectedSacrifices :+ (row, col)
                boardView.updateSacrificeHighlights(selectedSacrifices)
                println(s"FightView: sacrifices as of now: $selectedSacrifices")
              else
                println(s"FightView: enough blood already, choose a slot to place the card.")
            else
              println(s"FightView: not enough blood, currently: $amount.")
          case _ =>
            if isSlotEmpty then
              viewModel.cardToPlay(card, col)
              resetSelection()

  private def resetSelection(): Unit =
    selectedCard = None
    selectedSacrifices = List.empty
    boardView.updateSacrificeHighlights(selectedSacrifices)


  // To fill empty borders of board
  opaque = true
  background = new Color(20, 20, 22)
  val boardView = new BoardView(onSlotClicked)

  val handView = new HandView(onCardSelectedFromHand)

  val statsView = new StatsView(viewModel)

  val decksView = new DecksView(viewModel)

  layout(boardView) = BorderPanel.Position.Center
  layout(handView) = BorderPanel.Position.South
  layout(statsView) = BorderPanel.Position.West
  layout(decksView) = BorderPanel.Position.East





  private def updateViews(fightState: FightState, turn: TurnState): Unit =
      Swing.onEDT {
        val isPlayerActive = turn == TurnState.draw || turn == TurnState.playerTurn
        boardView.interactable = isPlayerActive
        handView.interactable = isPlayerActive
        statsView.interactable = isPlayerActive
        decksView.interactable = isPlayerActive
        if !isPlayerActive then resetSelection()
        currentBoard = Some(fightState.board)
        boardView.updateBoard(fightState.board)
        decksView.updateDeck(fightState.deck)
        handView.updateHand(fightState.playerHand.toList)
        statsView.updateScale(fightState.scalePoints)
        statsView.updateBones(fightState.bones)
        statsView.updateTurn(turn)
      }
