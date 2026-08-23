package org.scryption.view.fight

import org.scryption.game.model.Deck.Deck
import org.scryption.game.model.items.GameItem
import org.scryption.view.ViewModelFight
import org.scryption.view.common.ResourceLoader

import scala.swing.*
import scala.swing.event.{MouseClicked, MouseExited, MouseMoved}
import java.awt.{Color, Cursor, Dimension, Font}

class DecksView(viewModel : ViewModelFight, onItemClicked: GameItem => Unit) extends BorderPanel:

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

  private def loadItemIcon(path: String): javax.swing.ImageIcon =
    ResourceLoader.loadTemplateImage(path) match
      case Some(img) =>
        val size = 80
        val scaled = img.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        new javax.swing.ImageIcon(scaled)
      case None =>
        new javax.swing.ImageIcon()

  private class ItemLabel(val item: GameItem, imagePath: String) extends Label:
    icon = loadItemIcon(imagePath)
    tooltip = item.description
    cursor = new Cursor(Cursor.HAND_CURSOR)
    if icon.getIconWidth == -1 then text = item.name
    private var isHovered = false
    listenTo(mouse.clicks, mouse.moves)
    reactions += {
      case _: MouseMoved =>
        if interactable && !isHovered then
          isHovered = true
          repaint()
      case _: MouseExited =>
        isHovered = false
        repaint()
      case _: MouseClicked =>
        if interactable then onItemClicked(item)
    }

    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
      if isHovered then
        g.setColor(new Color(120, 120, 120, 150))
      else
        g.setColor(new Color(60, 60, 65, 100))
      g.fillRoundRect(5, 5, size.width - 10, size.height - 10, 20, 20)
      super.paintComponent(g)

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

  private val topPanel = new BoxPanel(Orientation.Vertical):
    opaque = false
    contents += mainDeckLabel
    contents += Swing.VStrut(30)
    contents += squirrelDeckLabel
    contents += Swing.VStrut(30)
    contents += new Label("ITEMS"):
      foreground = new Color(150, 150, 150)
      font = ResourceLoader.loadFont(fontPath, 20f)
    contents += Swing.VStrut(10)

  private val scrollItems = new ScrollPane(itemsContainer):
    opaque = false
    peer.getViewport.setOpaque(false)
    border = Swing.EmptyBorder(0)
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
    verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded

  layout(topPanel) = BorderPanel.Position.North
  layout(scrollItems) = BorderPanel.Position.Center

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

  /** Updates the main deck on the board.
   *
   * @param deck The main deck to updated the view with.
   */
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

  /** Updates th items on the board.
   *
   * @param items The list of items to update the board with.
   */
  def updateItems(items: List[GameItem]): Unit =
    itemsContainer.contents.clear()
    for item <- items do
      val imagePath = item.name match
        case "Squirrel in a Bottle" => "items/squirrel_bottle.png"
        case "Hoggy Bank"           => "items/hoggy_bank.png"
        case "Pliers"               => "items/pliers.png"
        case "Scissors"             => "items/scissors.png"
        case default                => s"items/${default.toLowerCase.replace(" ", "_")}.png"
      val itemLabel = new ItemLabel(item, imagePath)
      itemsContainer.contents += itemLabel
      itemsContainer.contents += Swing.VStrut(15)

    revalidate()
    repaint()
