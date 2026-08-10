package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.view.ResourceLoader
import scala.swing.*
import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage
import javax.swing.border.LineBorder

class BoardView(channel: GUIChannelInterface) extends BorderPanel:

  opaque = false
  private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")
  override protected def paintComponent(g: Graphics2D): Unit =
    backgroundImage.foreach { img =>
      g.drawImage(img, 0, 0, size.width, size.height, peer)
    }
    super.paintComponent(g)

  private val gridPanel = new GridPanel(3, 4):
    opaque = false
    hGap = 20
    vGap = 20
    border = Swing.EmptyBorder(50, 100, 50, 100)

  private val slots: Array[Label] = Array.fill(12):
    new Label:
      border = LineBorder(new Color(255, 255, 255, 80), 2, true)
      opaque = false

  slots.foreach(gridPanel.contents += _)
  layout(gridPanel) = BorderPanel.Position.Center