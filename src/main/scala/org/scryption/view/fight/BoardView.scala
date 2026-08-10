package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.game.model.boardModel.*
import org.scryption.view.{CardGeometry, CardView, CardViewAssets, ResourceLoader, toViewInfo}
import scala.swing.*
import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage
import javax.swing.border.LineBorder

class BoardView(channel: GUIChannelInterface) extends BorderPanel:

  opaque = false
  private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")

  // Setting proportions of the cards
  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val geometry = CardGeometry(cardWidth = 180)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

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

  private val slots: Array[Label] = Array.fill(RowsCount * ColsCount):
    new Label:
      border = LineBorder(new Color(255, 255, 255, 80), 2, true)
      opaque = false
      horizontalAlignment = Alignment.Center
      verticalAlignment = Alignment.Center

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
              case Some(icon) =>
                slots(slotIndex).icon = icon
                slots(slotIndex).border = Swing.EmptyBorder
              case None =>
                println(s"Error in the card rendering: ${card.name}")
          case None =>
            slots(slotIndex).icon = null
            slots(slotIndex).border = LineBorder(new Color(255, 255, 255, 80), 2, true)

    revalidate()
    repaint()