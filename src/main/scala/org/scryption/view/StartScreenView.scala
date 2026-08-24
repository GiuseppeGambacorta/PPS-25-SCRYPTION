package org.scryption.view

import org.scryption.view.common.GUIAssets.StartScreenViewAssets
import org.scryption.view.common.GUIGeometry.StartScreenGeometry
import org.scryption.view.common.ResourceLoader

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.awt.{Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel}
import scala.swing.{FlowPanel, Panel}
import scala.swing.event.UIElementResized

class StartScreenView(val geo: StartScreenGeometry, onNewGame: () => Unit, onLoadGame: () => Unit, onQuit: () => Unit)
    extends FlowPanel:

  val assets = StartScreenViewAssets()

  private def scaledIcon(img: BufferedImage, w: Int, h: Int): ImageIcon =
    new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH))

  private def loadIcon(path: String, w: Int, h: Int): Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(path).map(img => scaledIcon(img, w, h))

  private val introBackgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage(assets.startScreenPath)
  private val menuBackgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage(assets.menuBackgroundPath)

  private val slotImage: Option[ImageIcon] =
    loadIcon(assets.menuSlotPath, geo.slotWidth, geo.slotHeight)
  private val slotHighlightedImage: Option[ImageIcon] =
    loadIcon(assets.menuHighlightedSlotPath, geo.slotWidth, geo.slotHeight)

  private case class MenuCardDef(
      id: String,
      cardIcon: ImageIcon,
      textIcon: Option[ImageIcon],
      onChosen: () => Unit
  )

  private val menuDefs: Vector[MenuCardDef] = Vector(
    MenuCardDef(
      "newgame",
      loadIcon(assets.menuCardPath("newgame"), geo.buttonWidth, geo.buttonHeight).orNull,
      loadIcon(assets.menuTextPath("newgame"), geo.textWidth, geo.textHeight),
      () => startGame()
    ),
    MenuCardDef(
      "continue",
      loadIcon(assets.menuCardPath("continue"), geo.buttonWidth, geo.buttonHeight).orNull,
      loadIcon(assets.menuTextPath("continue"), geo.textWidth, geo.textHeight),
      () => onLoadGame()
    ),
    MenuCardDef(
      "startscreen",
      loadIcon(assets.menuCardPath("startscreen"), geo.buttonWidth, geo.buttonHeight).orNull,
      loadIcon(assets.menuTextPath("quit"), geo.textWidth, geo.textHeight),
      () => quitGame()
    )
  )

  // ----- State -----
  private var menuVisible: Boolean = false
  private var slotCardId: Option[String] = None
  private var hoveredButtonId: Option[String] = None
  private var slotHovered: Boolean = false
  private var buttons: Vector[MenuButton] = Vector.empty

  opaque = false
  peer.setLayout(null)
  focusable = true

  listenTo(this)
  reactions += { case UIElementResized(_) =>
    if (menuVisible) updateMenuLayout()
  }

  peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (!menuVisible) showMenu()
    }
  })

  private val textLabel: JLabel = new JLabel() {
    setOpaque(false)
    setVisible(false)
    override def paintComponent(g: java.awt.Graphics): Unit = {
      if (getIcon != null) g.drawImage(getIcon.asInstanceOf[ImageIcon].getImage, 0, 0, getWidth, getHeight, null)
    }
  }

  private val slotLabel: JLabel = new JLabel() {
    setOpaque(false)
    setCursor(new Cursor(Cursor.HAND_CURSOR))
    setVisible(false)
    override def paintComponent(g: java.awt.Graphics): Unit = {
      if (getIcon != null) g.drawImage(getIcon.asInstanceOf[ImageIcon].getImage, 0, 0, getWidth, getHeight, null)
    }
  }

  slotLabel.addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent): Unit = {
      slotHovered = true
      refreshSlotIcon()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      slotHovered = false
      refreshSlotIcon()
    }
  })

  peer.add(textLabel)
  peer.add(slotLabel)

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    val bg = if (menuVisible) menuBackgroundImage else introBackgroundImage
    bg.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))
  }

  private def showMenu(): Unit = {
    menuVisible = true
    buildMenu()
    revalidate()
    repaint()
  }

  private def buildMenu(): Unit =
    if (buttons.isEmpty) {
      buttons = menuDefs.map { cardDef =>
        val btn = new MenuButton(cardDef, 0, 0)
        peer.add(btn.label)
        btn
      }
    }
    textLabel.setVisible(true)
    slotLabel.setIcon(slotImage.orNull)
    slotLabel.setVisible(true)
    updateMenuLayout()

  private def updateMenuLayout(): Unit =
    val w = size.width
    val h = size.height

    val scale = Math.min(1.0, w.toDouble / 1920.0)
    val scaledBtnW = (geo.buttonWidth * scale).toInt
    val scaledBtnH = (geo.buttonHeight * scale).toInt
    val scaledGap = (geo.buttonGap * scale).toInt
    val scaledTextW = (geo.textWidth * scale).toInt
    val scaledTextH = (geo.textHeight * scale).toInt
    val scaledSlotW = (geo.slotWidth * scale).toInt
    val scaledSlotH = (geo.slotHeight * scale).toInt
    val textY = (h * 0.15).toInt
    val slotY = (h * 0.35).toInt
    val buttonsY = (h * 0.70).toInt

    textLabel.setBounds((w - scaledTextW) / 2, textY, scaledTextW, scaledTextH)
    slotLabel.setBounds((w - scaledSlotW) / 2, slotY, scaledSlotW, scaledSlotH)
    val totalButtonsWidth = menuDefs.length * scaledBtnW + (menuDefs.length - 1) * scaledGap
    val startX = (w - totalButtonsWidth) / 2
    buttons.zipWithIndex.foreach { case (btn, i) =>
      val newX = startX + i * (scaledBtnW + scaledGap)
      btn.updatePosition(newX, buttonsY, scaledBtnW, scaledBtnH, scaledSlotW, scaledSlotH)
    }

    refreshTextPreview()
    refreshSlotIcon()
    peer.revalidate()
    peer.repaint()

  private class MenuButton(var cardDef: MenuCardDef, var baseX: Int, var baseY: Int) {

    private var isInSlot: Boolean = false

    val label: JLabel = new JLabel(cardDef.cardIcon) {
      setBounds(baseX, baseY, geo.buttonWidth, geo.buttonHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
      override def paintComponent(g: java.awt.Graphics): Unit = {
        if (getIcon != null) g.drawImage(getIcon.asInstanceOf[ImageIcon].getImage, 0, 0, getWidth, getHeight, null)
      }
    }

    def updatePosition(newBaseX: Int, newBaseY: Int, w: Int, h: Int, slotW: Int, slotH: Int): Unit =
      baseX = newBaseX
      baseY = newBaseY
      label.setSize(w, h)
      if (isInSlot) {
        val slotX = slotLabel.getX + (slotW - w) / 2
        val slotY = slotLabel.getY + (slotH - h) / 2
        label.setLocation(slotX, slotY)
      } else {
        label.setLocation(baseX, baseY)
      }

    label.addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent): Unit = {
        if (isInSlot) {
          slotHovered = true
          refreshSlotIcon()
        } else {
          hoveredButtonId = Some(cardDef.id)
          refreshTextPreview()
        }
      }

      override def mouseExited(e: MouseEvent): Unit = {
        if (isInSlot) {
          slotHovered = false
          refreshSlotIcon()
        } else if (hoveredButtonId.contains(cardDef.id)) {
          hoveredButtonId = None
          refreshTextPreview()
        }
      }

      override def mouseClicked(e: MouseEvent): Unit = {
        if (isInSlot) {
          cardDef.onChosen()
        } else {
          moveToSlot()
        }
      }
    })

    private def moveToSlot(): Unit = {
      slotCardId.foreach { previousId =>
        buttons.find(_.cardDef.id == previousId).foreach(_.returnToOriginalPosition())
      }

      val slotX = slotLabel.getX + (geo.slotWidth - geo.buttonWidth) / 2
      val slotY = slotLabel.getY + (geo.slotHeight - geo.buttonHeight) / 2
      isInSlot = true
      label.setLocation(slotX, slotY)
      label.getParent.setComponentZOrder(label, 0)

      slotCardId = Some(cardDef.id)
      hoveredButtonId = None
      slotHovered = true
      refreshTextPreview()
      refreshSlotIcon()
      label.getParent.repaint()
    }

    private def returnToOriginalPosition(): Unit = {
      isInSlot = false
      label.setLocation(baseX, baseY)
      label.getParent.repaint()
    }
  }

  private def refreshTextPreview(): Unit = {
    val icon = slotCardId
      .orElse(hoveredButtonId)
      .flatMap(id => menuDefs.find(_.id == id))
      .flatMap(_.textIcon)
    textLabel.setIcon(icon.orNull)
    textLabel.repaint()
  }

  private def refreshSlotIcon(): Unit = {
    val icon = if (slotHovered) slotHighlightedImage else slotImage
    slotLabel.setIcon(icon.orNull)
    slotLabel.repaint()
  }

  private def startGame(): Unit = {
    onNewGame()
  }

  private def quitGame(): Unit = {
    onQuit()
  }
