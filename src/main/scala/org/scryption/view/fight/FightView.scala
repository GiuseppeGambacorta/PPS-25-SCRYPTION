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
  val statsPanel = new BoxPanel(Orientation.Vertical):
    opaque = true
    background = new Color(25, 25, 30)
    preferredSize = new Dimension(220, 0)
    border = Swing.EmptyBorder(20, 20, 20, 20)
    contents += new Label("Damage Scale") { foreground = Color.WHITE }
    contents += Swing.VStrut(50)
    contents += new Label("Bones: 0") { foreground = Color.WHITE }

  // Placeholder for decks and end turn button
  val decksPanel = new BoxPanel(Orientation.Vertical):
    opaque = true
    background = new Color(25, 25, 30)
    preferredSize = new Dimension(220, 0)
    border = Swing.EmptyBorder(20, 20, 20, 20)
    contents += new Label("Main Deck") { foreground = Color.WHITE }
    contents += Swing.VStrut(20)
    contents += new Label("Squirrels") { foreground = Color.WHITE }
    contents += Swing.VStrut(100)
    contents += new Button("Pass Turn")

  layout(boardView)  = BorderPanel.Position.Center
  layout(handPanel)  = BorderPanel.Position.South
  layout(statsPanel) = BorderPanel.Position.West
  layout(decksPanel) = BorderPanel.Position.East