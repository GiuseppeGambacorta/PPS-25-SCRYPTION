package org.scryption.view

import org.scryption.view.CardViewAssets
import org.scryption.view.ResourceLoader

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.AlphaComposite
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

  // TODO: move into CardViewAssets alongside the other path helpers once confirmed
  private val sigilPatchPath: String = "sigils/sigil_patch.png"

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
      drawEmission(g2d, card.name, card.addedSigils)
      drawCost(g2d, card.cost)
      drawName(g2d, card.name)
      drawStats(g2d, card.attack, card.health)
      drawSigils(g2d, card.defaultSigils)
      drawAddedSigils(g2d, card.defaultSigils, card.addedSigils)

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


  private def drawEmission(g2d: Graphics2D, name: String, addedSigils: List[String]): Unit =
    if (name.nonEmpty && addedSigils.nonEmpty) {
      val emissionPath = CardViewAssets.portraitPath(s"${name}_emission")
      ResourceLoader.loadImage(emissionPath, geometry.portraitSize).foreach { img =>
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

  private def drawSigils(g2d: Graphics2D, defaultSigils: List[String]): Unit = defaultSigils match {
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

  private def drawAddedSigils(g2d: Graphics2D, defaultSigils: List[String], addedSigils: List[String]): Unit =
    if (addedSigils.nonEmpty) {
      val slots: Vector[(Int, Int)] =
        if (defaultSigils.isEmpty)
          (geometry.patchCenterX, geometry.patchCenterY) +: geometry.addedSigilSlotCenters
        else
          geometry.addedSigilSlotCenters

      addedSigils.zip(slots).foreach { case (sigilName, (centerX, centerY)) =>
        drawAddedSigil(g2d, sigilName, centerX, centerY)
      }
    }

  private def drawAddedSigil(g2d: Graphics2D, sigilName: String, centerX: Int, centerY: Int): Unit = {
    val patchSize = geometry.patchSize
    val sigilSize = geometry.addedSigilSize

    val patchX = centerX - patchSize / 2
    val patchY = centerY - patchSize / 2
    val sigilX = centerX - sigilSize / 2
    val sigilY = centerY - sigilSize / 2

    ResourceLoader.loadImage(sigilPatchPath, patchSize).foreach { patch =>
      g2d.drawImage(patch, patchX, patchY, patchSize, patchSize, NoObserver)
    }

    ResourceLoader.loadImage(CardViewAssets.sigilPath(sigilName), sigilSize).foreach { sigilImg =>
      val lightened = emission(toBufferedImage(sigilImg, sigilSize, sigilSize))
      g2d.drawImage(lightened, sigilX, sigilY, sigilSize, sigilSize, NoObserver)
    }
  }

  private def emission(img: BufferedImage): BufferedImage = {
    val out = new BufferedImage(img.getWidth, img.getHeight, BufferedImage.TYPE_INT_ARGB)
    val g = out.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.drawImage(img, 0, 0, NoObserver)
    g.setComposite(AlphaComposite.SrcAtop)
    g.setColor(Color.decode("#85fcc3"))
    g.fillRect(0, 0, img.getWidth, img.getHeight)
    g.dispose()
    out
  }

  private def toBufferedImage(img: java.awt.Image, w: Int, h: Int): BufferedImage =
    img match {
      case bi: BufferedImage => bi
      case other =>
        val bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = bimg.createGraphics()
        g.drawImage(other, 0, 0, w, h, NoObserver)
        g.dispose()
        bimg
    }
}