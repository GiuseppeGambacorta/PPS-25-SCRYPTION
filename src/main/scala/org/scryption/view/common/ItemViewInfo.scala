package org.scryption.view.common

import org.scryption.game.model.items.GameItem

import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import scala.annotation.targetName

case class ItemViewInfo(name: String, description: String)

extension (item: GameItem)
  @targetName("itemToViewInfo")
  def itemToViewInfo: ItemViewInfo = ItemViewInfo(item.name, item.description)

object ItemView {

  /** Genera proceduralmente un'icona/carta per l'item con stile pergamena/legno scuro */
  def render(info: ItemViewInfo, width: Int, height: Int): ImageIcon = {
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    // Sfondo pergamena scura / cartone ruvido
    g.setColor(new Color(42, 35, 28))
    g.fillRoundRect(0, 0, width, height, 16, 16)

    // Bordo interno stilizzato
    g.setColor(new Color(180, 140, 90))
    g.setStroke(new BasicStroke(3f))
    g.drawRoundRect(4, 4, width - 8, height - 8, 12, 12)

    // Header / Box del Titolo
    g.setColor(new Color(28, 22, 16))
    g.fillRect(8, 12, width - 16, 36)

    // Nome dell'Item
    g.setColor(new Color(235, 210, 160))
    g.setFont(new Font("Monospaced", Font.BOLD, 13))
    val fontMetrics = g.getFontMetrics
    val titleX = (width - fontMetrics.stringWidth(info.name)) / 2
    g.drawString(info.name, titleX.max(12), 35)

    // Icona simbolica generata in mezzo (un cerchio/sigillo runico)
    g.setColor(new Color(70, 55, 40))
    g.fillOval(width / 2 - 25, 60, 50, 50)
    g.setColor(new Color(200, 165, 110))
    g.setStroke(new BasicStroke(2f))
    g.drawOval(width / 2 - 25, 60, 50, 50)
    g.setFont(new Font("Serif", Font.BOLD, 26))
    val initial = info.name.headOption.map(_.toString).getOrElse("?")
    g.drawString(initial, width / 2 - 8, 95)

    // Descrizione con Word-Wrap
    g.setColor(new Color(210, 195, 175))
    g.setFont(new Font("SansSerif", Font.PLAIN, 11))
    drawWrappedText(g, info.description, 14, 135, width - 28)

    g.dispose()
    new ImageIcon(image)
  }

  private def drawWrappedText(g: Graphics2D, text: String, x: Int, startY: Int, maxWidth: Int): Unit = {
    val fm = g.getFontMetrics
    val lineHeight = fm.getHeight
    val words = text.split(" ")
    var currentLine = ""
    var y = startY

    for (word <- words) {
      val testLine = if (currentLine.isEmpty) word else s"$currentLine $word"
      if (fm.stringWidth(testLine) > maxWidth) {
        g.drawString(currentLine, x, y)
        currentLine = word
        y += lineHeight
      } else {
        currentLine = testLine
      }
    }
    if (currentLine.nonEmpty) {
      g.drawString(currentLine, x, y)
    }
  }
}