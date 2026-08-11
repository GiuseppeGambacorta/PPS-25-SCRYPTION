package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.game.model.boardModel.*
import org.scryption.view.{CardGeometry, CardView, CardViewAssets, ResourceLoader, toViewInfo}
import scala.swing.*
import scala.swing.event.MouseClicked
import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage
import java.awt.Cursor

class BoardView(channel: GUIChannelInterface) extends BorderPanel:

  opaque = false
  private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val geometry = CardGeometry(cardWidth = 180)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  private def loadSlotIcon(path: String): javax.swing.ImageIcon =
    ResourceLoader.loadTemplateImage(path) match
      case Some(img) =>
        val height = (geometry.cardWidth * 1.52).toInt
        val scaled = img.getScaledInstance(geometry.cardWidth, height, java.awt.Image.SCALE_SMOOTH)
        new javax.swing.ImageIcon(scaled)
      case None =>
        new javax.swing.ImageIcon()

  private val iconBotPrep = loadSlotIcon("board/slot_bot_prep.png")
  private val iconBotAttack = loadSlotIcon("board/slot_bot_attack.png")
  private val iconPlayer = loadSlotIcon("board/slot_player.png")

  private def getDefaultIcon(row: Int): javax.swing.ImageIcon = row match
    case 0 => iconBotPrep
    case 1 => iconBotAttack
    case 2 => iconPlayer
    case _ => new javax.swing.ImageIcon()

  override protected def paintComponent(g: Graphics2D): Unit =
    backgroundImage.foreach { img =>
      g.drawImage(img, 0, 0, size.width, size.height, peer)
    }
    super.paintComponent(g)

  private val gridPanel = new GridPanel(RowsCount, ColsCount):
    opaque = false
    hGap = 15
    vGap = 15
    border = Swing.EmptyBorder(40, 80, 40, 80)

  private val slots: Array[Label] = Array.tabulate(RowsCount * ColsCount): index =>
    val row = index / ColsCount
    val col = index % ColsCount

    val slotLabel = new Label:
      icon = getDefaultIcon(row)
      opaque = false
      horizontalAlignment = Alignment.Center
      verticalAlignment = Alignment.Center

      if row == 2 then cursor = new Cursor(Cursor.HAND_CURSOR)

    listenTo(slotLabel.mouse.clicks)
    reactions += {
      case MouseClicked(`slotLabel`, _, _, _, _) =>
        if row == 2 then
          println(s"UI input: the player has clicked on a player slot (row: $row, column: $col)")
      //channel.sendToGame(GUIMessages.InteractWithBoard(row, col))
    }

    slotLabel

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
            val viewInfo = card.toViewInfo
            val template = CardViewAssets.frontTemplatePath(viewInfo.cardType)
            renderer.render(viewInfo, template) match
              case Some(cardIcon) =>
                slots(slotIndex).icon = cardIcon
              case None =>
                println(s"Error in the card rendering: ${card.name}")
          case None =>
            slots(slotIndex).icon = getDefaultIcon(row)

    revalidate()
    repaint()