package org.scryption.view

import org.scryption.view.CardViewAssets
import org.scryption.view.ResourceLoader

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

/** Draws a [[Card]] onto a template image using a fixed [[CardGeometry]].
  *
  * @param geometry
  *   size and position of card elements
  * @param nameFont
  *   custom Heavyweight font
  * @param statFont
  *   same font but slightly bigger
  */

class CardView(geometry: CardGeometry, nameFont: Font, statFont: Font) {

  private val NoObserver: java.awt.image.ImageObserver = null

  /** Renders `card` onto the template at `templatePath`. Returns None only if the template image itself can't be
    * loaded.
    */
  def render(card: CardViewInfo, templatePath: String): Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(templatePath).map { template =>
      val buffer = new BufferedImage(geometry.cardWidth, geometry.cardHeight, BufferedImage.TYPE_INT_RGB)
      val g2d = buffer.createGraphics()

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

      g2d.drawImage(template, 0, 0, geometry.cardWidth, geometry.cardHeight, NoObserver)

      drawPortrait(g2d, card.name)
      drawCost(g2d, card.cost)
      drawName(g2d, card.name)
      drawStats(g2d, card.attack, card.health)
      drawSigils(g2d, card.sigils)

      g2d.dispose()
      new ImageIcon(buffer)
    }

  private def drawPortrait(g2d: Graphics2D, name: String): Unit =
    if (name.nonEmpty) {
      ResourceLoader.loadImage(CardViewAssets.portraitPath(name), geometry.portraitSize).foreach { img =>
        g2d.drawImage(
          img,
          geometry.portraitX,
          geometry.portraitY,
          geometry.portraitSize,
          geometry.portraitSize,
          NoObserver
        )
      }
    }

  private def drawCost(g2d: Graphics2D, costCode: String): Unit =
    ResourceLoader.loadImage(CardViewAssets.costIconPath(costCode), geometry.costSize) match {
      case Some(img) =>
        g2d.drawImage(img, geometry.costX, geometry.costY, geometry.costSize, geometry.costSize, NoObserver)
      case None =>
        // No icon for this cost code yet — fall back to plain text so the
        // card is still readable instead of showing a blank corner.
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20))
        g2d.setColor(Color.BLACK)
        g2d.drawString(costCode, 10, 30)
    }

  private def drawName(g2d: Graphics2D, name: String): Unit =
    if (name.nonEmpty) {
      g2d.setFont(nameFont)
      g2d.setColor(Color.BLACK)
      val textWidth = g2d.getFontMetrics.stringWidth(name)
      val nameX = (geometry.cardWidth - textWidth) / 2
      g2d.drawString(name, nameX, geometry.nameY)
    }

  private def drawStats(g2d: Graphics2D, attack: String, health: String): Unit = {
    g2d.setFont(statFont)
    g2d.setColor(Color.BLACK)
    if (attack.nonEmpty) g2d.drawString(attack, geometry.attackX, geometry.attackY)
    if (health.nonEmpty) g2d.drawString(health, geometry.healthX, geometry.healthY)
  }

  private def drawSigils(g2d: Graphics2D, sigils: List[String]): Unit = sigils match {
    case single :: Nil =>
      ResourceLoader.loadImage(CardViewAssets.sigilPath(single), geometry.sigilSize).foreach { img =>
        g2d.drawImage(img, geometry.sigilCenterX, geometry.sigilCenterY, NoObserver)
      }
    case left :: right :: _ =>
      ResourceLoader.loadImage(CardViewAssets.sigilPath(left), geometry.twinSigilSize).foreach { img =>
        g2d.drawImage(img, geometry.sigilLeftX, geometry.sigilLeftY - geometry.sigilSize, NoObserver)
      }
      ResourceLoader.loadImage(CardViewAssets.sigilPath(right), geometry.twinSigilSize).foreach { img =>
        g2d.drawImage(img, geometry.sigilRightX, geometry.sigilRightY - geometry.sigilSize, NoObserver)
      }
    case Nil => ()
  }
}
