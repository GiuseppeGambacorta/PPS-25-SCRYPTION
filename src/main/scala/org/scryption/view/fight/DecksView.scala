package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.view.ResourceLoader
import scala.swing.*
import scala.swing.event.{ButtonClicked, MouseClicked}
import java.awt.{Color, Cursor, Dimension, Font}

class DecksView(channel: GUIChannelInterface) extends BoxPanel(Orientation.Vertical):

  opaque = true
  background = new Color(25, 25, 30)
  preferredSize = new Dimension(220, 0)
  border = Swing.EmptyBorder(20, 40, 20, 40)

  private def loadDeckIcon(path: String): javax.swing.ImageIcon =
    ResourceLoader.loadTemplateImage(path) match
      case Some(img) =>
        val width = 140
        val height = (width * 1.52).toInt
        val scaled = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
        new javax.swing.ImageIcon(scaled)
      case None =>
        new javax.swing.ImageIcon()

  val mainDeckLabel = new Label:
    icon = loadDeckIcon("cardtemplates/back.png")
    tooltip = "Draw from Main Deck"
    cursor = new Cursor(Cursor.HAND_CURSOR)

  val squirrelDeckLabel = new Label:
    icon = loadDeckIcon("cardtemplates/squirrel_back.png")
    tooltip = "Draw a Squirrel"
    cursor = new Cursor(Cursor.HAND_CURSOR)

  val endTurnButton = new Button("End Turn"):
    font = new Font("SansSerif", Font.BOLD, 16)
    background = new Color(150, 50, 50)
    foreground = Color.WHITE
    cursor = new Cursor(Cursor.HAND_CURSOR)

  contents += mainDeckLabel
  contents += Swing.VStrut(30)
  contents += squirrelDeckLabel
  contents += Swing.VGlue
  contents += endTurnButton

  listenTo(mainDeckLabel.mouse.clicks, squirrelDeckLabel.mouse.clicks, endTurnButton)

  reactions += {
    case MouseClicked(`mainDeckLabel`, _, _, _, _) =>
      println("UI input: player draws from main deck")
    //channel.sendToGame(GUIMessages.DrawMainDeck)

    case MouseClicked(`squirrelDeckLabel`, _, _, _, _) =>
      println("UI input: player draws a squirrel")
    //channel.sendToGame(GUIMessages.DrawSquirrel)

    case ButtonClicked(`endTurnButton`) =>
      println("UI input: the player ends the turn")
    //channel.sendToGame(GUIMessages.EndTurn)
  }
