package org.scryption.view.common

import GUIAssets.CardViewAssets
import GUIGeometry.CardGeometry
import org.scryption.view.common.{CardView, ResourceLoader}

import java.awt.{AlphaComposite, Color, Font, Graphics2D, Image, RenderingHints}
import java.awt.image.{BufferedImage, ImageObserver}
import javax.swing.ImageIcon

class CardView(val geo: CardGeometry, val assets: CardViewAssets) {

  private val nameFont: Font = ResourceLoader.loadFont(assets.fontPath, geo.nameFontSize)
  private val statFont: Font = ResourceLoader.loadFont(assets.fontPath, geo.statFontSize)
  private val NoObserver: ImageObserver = null

  def render(card: CardViewInfo, templatePath: String): Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(templatePath).map { template =>
      val buffer = new BufferedImage(geo.cardWidth, geo.cardHeight, BufferedImage.TYPE_INT_RGB)
      val g2d = buffer.createGraphics()

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

      drawImage(g2d, template, 0, 0, geo.cardWidth, Some(geo.cardHeight))

      drawPortrait(g2d, card.name, card.addedSigils)
      drawCost(g2d, card.cost)
      drawName(g2d, card.name)
      drawStats(g2d, card.attack, card.health)
      drawSigils(g2d, card.defaultSigils, card.addedSigils)

      g2d.dispose()
      new ImageIcon(buffer)
    }


  private def drawImage(g2d: Graphics2D, img: Image, x: Int, y: Int, width: Int, height: Option[Int] = None): Unit =
    height match {
      case None    => g2d.drawImage(img, x, y, width, width, NoObserver)
      case Some(h) => g2d.drawImage(img, x, y, width, h, NoObserver)
    }

  private def drawPortrait(g2d: Graphics2D, name: String, addedSigils: List[String]): Unit =
    ResourceLoader.loadImage(assets.portraitPath(name), geo.portraitSize).foreach { img =>
      drawImage(g2d, img, geo.portraitX, geo.portraitY, geo.portraitSize)}

    if (addedSigils.nonEmpty) {
      ResourceLoader.loadImage(assets.emissionPath(name), geo.portraitSize).foreach { img =>
        drawImage(g2d, emission(img, geo.portraitSize), geo.portraitX, geo.portraitY, geo.portraitSize)
      }
    }

  private def drawCost(g2d: Graphics2D, costCode: String): Unit =
    ResourceLoader.loadImage(assets.costIconPath(costCode), geo.costSize) match {
      case Some(img) =>
        drawImage(g2d, img, geo.costX, geo.costY, geo.costSize)
      case None =>
        g2d.setFont(new Font("SansSerif", Font.BOLD, geo.nameFontSize))
        g2d.setColor(Color.BLACK)
        g2d.drawString(costCode, geo.costX, geo.costY)
    }

  private def drawName(g2d: Graphics2D, name: String): Unit =
    if (name.nonEmpty) {
      g2d.setFont(nameFont)
      g2d.setColor(Color.BLACK)
      val textWidth = g2d.getFontMetrics.stringWidth(name)
      val nameX = centerPos(geo.cardWidth, textWidth)
      g2d.drawString(name, nameX, geo.nameY)
    }
    
  private def centerPos(containerW: Int, objectW: Int): Int = (containerW - objectW) / 2 

  private def drawStats(g2d: Graphics2D, attack: String, health: String): Unit =
    g2d.setFont(statFont)
    g2d.setColor(Color.BLACK)
    if (attack.nonEmpty) g2d.drawString(attack, geo.attackX, geo.attackY)
    if (health.nonEmpty) g2d.drawString(health, geo.healthX, geo.healthY)

  private def drawSigils(g2d: Graphics2D, defaultSigils: List[String], addedSigils: List[String]): Unit = {

    defaultSigils match {
      case Nil => ()
      case single :: Nil =>
        ResourceLoader.loadImage(assets.sigilPath(single), geo.sigilSize).foreach { img =>
          drawImage(g2d, img, geo.sigilCenterX, geo.sigilCenterY, geo.sigilSize)
        }
      case left :: right :: _ =>
        ResourceLoader.loadImage(assets.sigilPath(left), geo.twinSigilSize).foreach { img =>
          drawImage(g2d, img, geo.sigilLeftX, geo.sigilLeftY - geo.sigilSize, geo.twinSigilSize)
        }
        ResourceLoader.loadImage(assets.sigilPath(right), geo.twinSigilSize).foreach { img =>
          drawImage(g2d, img, geo.sigilRightX, geo.sigilRightY - geo.sigilSize, geo.twinSigilSize)
        }
    }

    if (addedSigils.nonEmpty) {
      val slots: Vector[(Int, Int)] = defaultSigils match {
        case Nil   => (geo.patchCenterX, geo.patchCenterY) +: geo.addedSigilSlotCenters
        case _     => geo.addedSigilSlotCenters
      }

      addedSigils.zip(slots).foreach {
        case (sigilName, (centerX, centerY)) =>
          ResourceLoader.loadImage(assets.sigilPatchPath, geo.patchSize).foreach { patch =>
            drawImage(g2d, patch, centerX - geo.patchSize/2 , centerY - geo.patchSize/2, geo.patchSize)
          }

          ResourceLoader.loadImage(assets.sigilPath(sigilName), geo.sigilSize).foreach { sigilImg =>
            drawImage(g2d, emission(sigilImg, geo.addedSigilSize), (centerX - geo.addedSigilSize / 2), (centerY - geo.addedSigilSize / 2), geo.addedSigilSize)
          }
      }
    }
  }

  private def emission(img: Image, size: Int): BufferedImage = {
    val out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = out.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.drawImage(img, 0, 0, size, size, NoObserver)
    g.setComposite(AlphaComposite.SrcAtop)
    g.setColor(Color.decode("#85fcc3"))
    g.fillRect(0, 0, size, size)
    g.dispose()
    out
  }
}

object CardView {
  def forWidth(cardWidth: Int): CardView = {
    val geometry = CardGeometry(cardWidth)
    val assets = CardViewAssets()
    new CardView(geometry, assets)
  }
}