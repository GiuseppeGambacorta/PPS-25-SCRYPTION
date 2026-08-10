package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import scala.swing.*
import java.awt.{Color, Dimension}

class FightView(channel: GUIChannelInterface) extends BorderPanel:

  // To fill empty borders of board
  opaque = true
  background = new Color(20, 20, 22)
  val boardView = new BoardView(channel)

  // Placeholder for the playerhand
  val handPanel = new FlowPanel:
    opaque = true
    background = new Color(30, 30, 35)
    preferredSize = new Dimension(0, 200)
    contents += new Label("Player's hand") { foreground = Color.WHITE }

  // Placeholder for the scale and bones counter
  val statsView = new StatsView(channel)

  // Placeholder for decks and end turn button
  val decksView = new DecksView(channel)

  layout(boardView) = BorderPanel.Position.Center
  layout(handPanel) = BorderPanel.Position.South
  layout(statsView) = BorderPanel.Position.West
  layout(decksView) = BorderPanel.Position.East