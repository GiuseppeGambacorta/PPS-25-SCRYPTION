package org.scryption.view.fight

import org.scryption.game.model.boardModel.*
import org.scryption.view.common.GUIGeometry.CardGeometry
import org.scryption.view.common.GUIAssets.CardViewAssets
import org.scryption.view.common.*

import scala.swing.*
import scala.swing.event.{MouseClicked, MouseExited, MouseMoved}
import java.awt.{BasicStroke, Color, Cursor, Graphics2D}
import java.awt.image.BufferedImage

class BoardView(onSlotClicked: (Int, Int) => Unit) extends BorderPanel:

  opaque = false
  private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")

  private val geometry = CardGeometry(cardWidth = 200)
  private val renderer = new CardView(geometry, new CardViewAssets)
  private var highlightedSacrifices: List[(Int, Int)] = List.empty
  var interactable = true
  private var flashingRow: Option[(Int, Color)] = None

  def flashAttackingRow(row: Int, color: Color): Unit =
    flashingRow = Some((row, color))
    repaint()
    val timer = new javax.swing.Timer(800, _ => {
      flashingRow = None
      repaint()
    })
    timer.setRepeats(false)
    timer.start()

  def updateSacrificeHighlights(sacrifices: List[(Int, Int)]): Unit =
    highlightedSacrifices = sacrifices
    repaint()
  
  private def loadSlotIcon(path: String): Option[java.awt.Image] =
    ResourceLoader.loadTemplateImage(path).map: img =>
      val height = (geometry.cardWidth * 1.52).toInt
      img.getScaledInstance(geometry.cardWidth, height, java.awt.Image.SCALE_SMOOTH)

  private val iconBotPrep = loadSlotIcon("board/slot_bot_prep.png")
  private val iconBotAttack = loadSlotIcon("board/slot_bot_attack.png")
  private val iconPlayer = loadSlotIcon("board/slot_player.png")

  private def getDefaultIcon(row: Int): Option[java.awt.Image] = row match
    case 0 => iconBotPrep
    case 1 => iconBotAttack
    case 2 => iconPlayer
    case _ => None

  override protected def paintComponent(g: Graphics2D): Unit =
    backgroundImage.foreach { img =>
      g.drawImage(img, 0, 0, size.width, size.height, peer)
    }
    super.paintComponent(g)

  private class ScalableSlot(val row: Int, val col: Int) extends Panel:
    opaque = false
    if row == 2 then cursor = new Cursor(Cursor.HAND_CURSOR)
    var currentImage: Option[java.awt.Image] = getDefaultIcon(row)
    var isHovered: Boolean = false

    private def getCardBounds: Rectangle =
      val aspect = 1.52
      var w = size.width
      var h = (w * aspect).toInt
      if h > size.height then
        h = size.height
        w = (h / aspect).toInt
      val x = (size.width - w) / 2
      val y = (size.height - h) / 2
      new Rectangle(x, y, w, h)

    listenTo(mouse.clicks, mouse.moves)
    reactions += {
      case e: MouseMoved =>
        if interactable then
          val bounds = getCardBounds
          val px = e.point.x
          val py = e.point.y
          val isNowHovered = checkCardBounds(bounds, px, py)
          if isNowHovered != isHovered then
            isHovered = isNowHovered
            if row == 2 && isNowHovered then cursor = new Cursor(Cursor.HAND_CURSOR)
            else cursor = Cursor.getDefaultCursor
            repaint()
      case MouseExited(_, _, _) =>
        isHovered = false
        cursor = Cursor.getDefaultCursor
        repaint()
      case e: MouseClicked =>
        if interactable then
          if row == 2 then println(s"UI input: the player has clicked on a player slot (row: $row, column: $col)")
          val bounds = getCardBounds
          val px = e.point.x; val py = e.point.y
          if checkCardBounds(bounds, px, py) then
            onSlotClicked(row, col)
    }

    private def checkCardBounds(bounds: Rectangle, px: Int, py: Int) = {
      px >= bounds.x && px <= (bounds.x + bounds.width) && py >= bounds.y && py <= (bounds.y + bounds.height)
    }

    override protected def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      val bounds = getCardBounds

      currentImage.foreach { img =>
        // interpolation to scale properly the image in smaller dimensions
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, null)
      }

      flashingRow match
        case Some((r, color)) if r == row && currentImage.isDefined && currentImage != getDefaultIcon(row) =>
          g.setStroke(new BasicStroke(6))
          g.setColor(color)
          g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1)
          g.setColor(new Color(color.getRed, color.getGreen, color.getBlue, 70))
          g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
        case _ =>
          if highlightedSacrifices.contains((row, col)) then
            g.setStroke(new BasicStroke(5))
            g.setColor(new Color(255, 30, 30))
            g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1)
          else if isHovered then
            g.setStroke(new BasicStroke(4))
            if row == 2 then g.setColor(new Color(100, 200, 255))
            else g.setColor(new Color(255, 100, 100))
            g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1)

  private val gridPanel = new GridPanel(RowsCount, ColsCount):
    opaque = false
    hGap = 15
    vGap = 15
    border = Swing.EmptyBorder(40, 80, 40, 80)

  private val slots: Array[ScalableSlot] = Array.tabulate(RowsCount * ColsCount): index =>
    val row = index / ColsCount
    val col = index % ColsCount
    new ScalableSlot(row, col)

  slots.foreach(gridPanel.contents += _)
  layout(gridPanel) = BorderPanel.Position.Center

  /** Updates all 12 slots based on the current state of the Board.
   */
  def updateBoard(board: Board): Unit =
    for row <- 0 until RowsCount do
      for col <- 0 until ColsCount do
        val slotIndex = row * ColsCount + col
        board(row)(col) match
          case Some(card) =>
            val viewInfo = card.cardToViewInfo
            val template = renderer.assets.frontTemplatePath(viewInfo.cardType)
            renderer.render(viewInfo, template) match
              case Some(cardIcon) =>
                slots(slotIndex).currentImage = Some(cardIcon.getImage)
              case None =>
                println(s"Error in the card rendering: ${card.name}")
          case None =>
            slots(slotIndex).currentImage = getDefaultIcon(row)

    revalidate()
    repaint()