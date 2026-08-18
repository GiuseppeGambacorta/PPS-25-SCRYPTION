package org.scryption.view.fight

import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.items.GameItem
import org.scryption.view.ViewModelFight
import org.scryption.view.common.ResourceLoader
import org.scryption.{FightMessages, GUIChannelInterface}

import scala.swing.*
import scala.swing.event.{ButtonClicked, MouseClicked}
import java.awt.{Color, Cursor, Dimension, Font}

class DecksView(viewModel : ViewModelFight, onItemClicked: GameItem => Unit) extends BoxPanel(Orientation.Vertical):

  var interactable = true
  private var isMainDeckEmpty = false
  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
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

  private val mainDeckLabel = new Label:
    icon = loadDeckIcon("cardtemplates/back.png")
    tooltip = "Draw from Main Deck"
    cursor = new Cursor(Cursor.HAND_CURSOR)

  private val squirrelDeckLabel = new Label:
    icon = loadDeckIcon("cardtemplates/squirrel_back.png")
    tooltip = "Draw a Squirrel"
    cursor = new Cursor(Cursor.HAND_CURSOR)

  private val itemsContainer = new BoxPanel(Orientation.Vertical):
    opaque = false

  contents += mainDeckLabel
  contents += Swing.VStrut(30)
  contents += squirrelDeckLabel
  contents += Swing.VGlue
  contents += new Label("ITEMS"):
    foreground = new Color(150, 150, 150)
    font = ResourceLoader.loadFont(fontPath, 20f)
  contents += Swing.VStrut(10)
  contents += itemsContainer

  listenTo(mainDeckLabel.mouse.clicks, squirrelDeckLabel.mouse.clicks)

  reactions += {
    case MouseClicked(`mainDeckLabel`, _, _, _, _) =>
      if interactable && !isMainDeckEmpty then
        println("UI input: player draws from main deck")
        viewModel.drawFromDeck()

    case MouseClicked(`squirrelDeckLabel`, _, _, _, _) =>
      if interactable then
        println("UI input: player draws a squirrel")
        viewModel.drawFromSquirrel()
  }

  def updateDeck(deck: Deck): Unit =
    isMainDeckEmpty = deck.isEmpty
    if isMainDeckEmpty then
      mainDeckLabel.icon = null
      mainDeckLabel.tooltip = "Deck is empty!"
      mainDeckLabel.cursor = Cursor.getDefaultCursor
    else
      mainDeckLabel.icon = loadDeckIcon("cardtemplates/back.png")
      mainDeckLabel.tooltip = "Draw from Main Deck"
      mainDeckLabel.cursor = new Cursor(Cursor.HAND_CURSOR)

  def updateItems(items: List[GameItem]): Unit =
    itemsContainer.contents.clear()

    for item <- items do
      val itemBtn = new Button(item.name):
        tooltip = item.description
        cursor = new Cursor(Cursor.HAND_CURSOR)
        background = new Color(60, 50, 50)
        foreground = Color.WHITE
        preferredSize = new Dimension(140, 40)
        maximumSize = new Dimension(140, 40)

      listenTo(itemBtn)
      reactions += {
        case ButtonClicked(`itemBtn`) =>
          if interactable then onItemClicked(item)
      }

      itemsContainer.contents += itemBtn
      itemsContainer.contents += Swing.VStrut(10)

    revalidate()
    repaint()
