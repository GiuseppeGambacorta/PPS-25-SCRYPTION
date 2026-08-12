package org.scryption.view.fight

import org.scryption.game.model.{Card, SacrificeAttribute}
import org.scryption.game.model.boardModel.Board
//import org.scryption.game.model.managers.SacrificeManager
import org.scryption.{GUIChannelInterface, GUIMessages}

import scala.swing.*
import java.awt.{Color, Dimension}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FightView(channel: GUIChannelInterface) extends BorderPanel:

  var selectedCard: Option[Card[?]] = None
  var selectedSacrifices: List[(Int, Int)] = List.empty
  var currentBoard: Option[Board] = None

  def onCardSelectedFromHand(card: Card[?]): Unit =
    selectedCard = Some(card)
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
              channel.sendToGame(GUIMessages.CardToPlayWithSacrifices(card, col, selectedSacrifices))
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
              channel.sendToGame(GUIMessages.CardToPlay(card, col))
              resetSelection()

  private def resetSelection(): Unit =
    selectedCard = None
    selectedSacrifices = List.empty
    boardView.updateSacrificeHighlights(selectedSacrifices)


  // To fill empty borders of board
  opaque = true
  background = new Color(20, 20, 22)
  val boardView = new BoardView(channel, onSlotClicked)

  val handView = new HandView(channel, onCardSelectedFromHand)

  val statsView = new StatsView(channel)

  val decksView = new DecksView(channel)

  layout(boardView) = BorderPanel.Position.Center
  layout(handView) = BorderPanel.Position.South
  layout(statsView) = BorderPanel.Position.West
  layout(decksView) = BorderPanel.Position.East

  listenToChannel()

  private def listenToChannel(): Unit = {
    Future {
      while (true) {
        val msg = channel.receiveFromGame
        msg match {
          case GUIMessages.FightState(fightState) =>
            Swing.onEDT {
              currentBoard = Some(fightState.board)
              boardView.updateBoard(fightState.board)
              decksView.updateDeck(fightState.deck)
              handView.updateHand(fightState.playerHand.toList)
              statsView.updateScale(fightState.scalePoints)
              statsView.updateBones(fightState.bones)
            }
          case _ =>
        }
      }
    }
  }
