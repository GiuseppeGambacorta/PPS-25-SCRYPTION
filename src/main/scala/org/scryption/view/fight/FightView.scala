package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import scala.swing.*
import java.awt.{Color, Dimension}

class FightView(channel: GUIChannelInterface) extends BorderPanel:

  // To fill empty borders of board
  opaque = true
  background = new Color(20, 20, 22)
  val boardView = new BoardView(channel)

  val handView = new HandView(channel)

  val statsView = new StatsView(channel)

  val decksView = new DecksView(channel)

  layout(boardView) = BorderPanel.Position.Center
  layout(handView) = BorderPanel.Position.South
  layout(statsView) = BorderPanel.Position.West
  layout(decksView) = BorderPanel.Position.East