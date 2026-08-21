package org.scryption.view

import org.scryption.view.common.GUIAssets.StartScreenViewAssets
import org.scryption.view.common.GUIGeometry.StartScreenGeometry
import org.scryption.view.common.ResourceLoader

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.awt.{Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel}
import scala.swing.{FlowPanel, Panel}

class StartScreenView(val geo: StartScreenGeometry, onNewGame: () => Unit, onLoadGame: () => Unit, onQuit: () => Unit)
    extends FlowPanel:

  val assets = StartScreenViewAssets()

  private def scaledIcon(img: BufferedImage, w: Int, h: Int): ImageIcon =
    new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH))

  private def loadIcon(path: String, w: Int, h: Int): Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(path).map(img => scaledIcon(img, w, h))

  // ----- Assets -----
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

  peer.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (!menuVisible) showMenu()
    }
  })

  private val textLabel: JLabel = new JLabel() {
    setOpaque(false)
    setVisible(false)
  }

  private val slotLabel: JLabel = new JLabel() {
    setOpaque(false)
    setCursor(new Cursor(Cursor.HAND_CURSOR))
    setVisible(false)
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

  private def buildMenu(): Unit = {
    val w = size.width
    val h = size.height

    textLabel.setBounds((w - geo.textWidth) / 2, geo.part1Y, geo.textWidth, geo.textHeight)
    textLabel.setVisible(true)

    slotLabel.setBounds((w - geo.slotWidth) / 2, geo.part2Y, geo.slotWidth, geo.slotHeight)
    slotLabel.setIcon(slotImage.orNull)
    slotLabel.setVisible(true)

    val totalButtonsWidth = menuDefs.length * geo.buttonWidth + (menuDefs.length - 1) * geo.buttonGap
    val startX = (w - totalButtonsWidth) / 2

    buttons = menuDefs.zipWithIndex.map { case (cardDef, i) =>
      val baseX = startX + i * (geo.buttonWidth + geo.buttonGap)
      val btn = new MenuButton(cardDef, baseX, geo.part3Y)
      peer.add(btn.label)
      btn
    }

    refreshTextPreview()
    refreshSlotIcon()
    peer.revalidate()
    peer.repaint()
  }

  private class MenuButton(val cardDef: MenuCardDef, val baseX: Int, val baseY: Int) {

    private var isInSlot: Boolean = false

    val label: JLabel = new JLabel(cardDef.cardIcon) {
      setBounds(baseX, baseY, geo.buttonWidth, geo.buttonHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
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
    val icon = slotCardId.orElse(hoveredButtonId)
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
