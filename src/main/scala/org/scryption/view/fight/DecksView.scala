package org.scryption.view.fight

import org.scryption.game.model.Deck.Deck
import org.scryption.{GUIChannelInterface, GUIMessages}
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

  contents += mainDeckLabel
  contents += Swing.VStrut(30)
  contents += squirrelDeckLabel
  contents += Swing.VGlue

  listenTo(mainDeckLabel.mouse.clicks, squirrelDeckLabel.mouse.clicks)

  reactions += {
    case MouseClicked(`mainDeckLabel`, _, _, _, _) =>
      println("UI input: player draws from main deck")
      channel.sendToGame(GUIMessages.DrawFromDeck)

    case MouseClicked(`squirrelDeckLabel`, _, _, _, _) =>
      println("UI input: player draws a squirrel")
      channel.sendToGame(GUIMessages.DrawFromSquirrel)
  }

  def updateDeck(deck: Deck): Unit =
    if deck.isEmpty then {
      println(s"[${deck.toList}]")
    }
