package org.scryption.view

import org.scryption.view.ResourceLoader
import org.scryption.{GUIChannelInterface, GUIMessages}

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.awt.{Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel}
import scala.swing.Panel

class StartScreenView(onNewGame: () => Unit, onQuit: () => Unit) extends Panel {

  // ----- Layout config: TUNE to match the real art -----
  // Must come before any val that uses these sizes.
  private val slotWidth = 48 * 5
  private val slotHeight = 62 * 5
  private val buttonWidth = 42 * 5
  private val buttonHeight = 56 * 5
  private val buttonGap = 80
  private val textWidth = 214 * 3
  private val textHeight = 28 * 3

  // ----- Icon scaling helpers -----
  private def scaledIcon(img: BufferedImage, w: Int, h: Int): ImageIcon =
    new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH))

  private def loadIcon(path: String, w: Int, h: Int): Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(path).map(img => scaledIcon(img, w, h))

  private def loadCardIcon(path: String): ImageIcon =
    loadIcon(path, buttonWidth, buttonHeight).orNull

  // ----- Assets -----
  private val introBackgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("menu/startscreen.png")
  private val menuBackgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("menu/startscreen_background_PART1.png")

  private val slotImage: Option[ImageIcon] =
    loadIcon("menu/startscreen_slot_PART1.png", slotWidth, slotHeight)
  private val slotHighlightedImage: Option[ImageIcon] =
    loadIcon("menu/startscreen_slot_highlighted_PART1.png", slotWidth, slotHeight)

  // ----- Menu definitions: add more entries here later, nothing else needs to change -----
  private case class MenuCardDef(
                                  id: String,
                                  cardIcon: ImageIcon,
                                  textIcon: Option[ImageIcon],
                                  onChosen: () => Unit
                                )

  private val menuDefs: Vector[MenuCardDef] = Vector(
    MenuCardDef(
      "newgame",
      loadCardIcon("menu/menucard_newgame.png"),
      loadIcon("menu/menutext_newgame.png", textWidth, textHeight),
      () => startGame()
    ),
    MenuCardDef(
      "startscreen",
      loadCardIcon("menu/menucard_startscreen.png"),
      loadIcon("menu/menutext_quit.png", textWidth, textHeight),
      () => quitGame()
    )
  )

  slotImage.foreach(icon => println(s"slot loaded size: ${icon.getIconWidth} x ${icon.getIconHeight}"))

  // ----- State -----
  private var menuVisible: Boolean = false
  private var slotCardId: Option[String] = None
  private var hoveredButtonId: Option[String] = None
  private var slotHovered: Boolean = false
  private var buttons: Vector[MenuButton] = Vector.empty

  opaque = false
  peer.setLayout(null)
  focusable = true
  preferredSize = new Dimension(1920, 1080)

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
    bg.foreach(img => g.drawImage(img, 0, 0, preferredSize.width, preferredSize.height, peer))
  }

  private def showMenu(): Unit = {
    menuVisible = true
    buildMenu()
    revalidate()
    repaint()
  }

  private def buildMenu(): Unit = {
    val w = preferredSize.width
    val h = preferredSize.height

    val part1Y = (h * 0.06).toInt
    val part2Y = (h * 0.3).toInt
    val part3Y = (h * 0.7).toInt

    textLabel.setBounds((w - textWidth) / 2, part1Y, textWidth, textHeight)
    textLabel.setVisible(true)

    slotLabel.setBounds((w - slotWidth) / 2, part2Y, slotWidth, slotHeight)
    slotLabel.setIcon(slotImage.orNull)
    slotLabel.setVisible(true)

    val totalButtonsWidth = menuDefs.length * buttonWidth + (menuDefs.length - 1) * buttonGap
    val startX = (w - totalButtonsWidth) / 2

    buttons = menuDefs.zipWithIndex.map { case (defn, i) =>
      val baseX = startX + i * (buttonWidth + buttonGap)
      val btn = new MenuButton(defn, baseX, part3Y)
      peer.add(btn.label)
      btn
    }

    refreshTextPreview()
    refreshSlotIcon()
    peer.revalidate()
    peer.repaint()
  }

  private class MenuButton(val defn: MenuCardDef, val baseX: Int, val baseY: Int) {

    var isInSlot: Boolean = false

    val label: JLabel = new JLabel(defn.cardIcon) {
      setBounds(baseX, baseY, buttonWidth, buttonHeight)
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
          hoveredButtonId = Some(defn.id)
          refreshTextPreview()
        }
      }

      override def mouseExited(e: MouseEvent): Unit = {
        if (isInSlot) {
          slotHovered = false
          refreshSlotIcon()
        } else if (hoveredButtonId.contains(defn.id)) {
          hoveredButtonId = None
          refreshTextPreview()
        }
      }

      override def mouseClicked(e: MouseEvent): Unit = {
        if (isInSlot) {
          defn.onChosen()
        } else {
          moveToSlot()
        }
      }
    })

    private def moveToSlot(): Unit = {
      slotCardId.foreach { previousId =>
        buttons.find(_.defn.id == previousId).foreach(_.returnToOriginalPosition())
      }

      val slotX = slotLabel.getX + (slotWidth - buttonWidth) / 2
      val slotY = slotLabel.getY + (slotHeight - buttonHeight) / 2
      isInSlot = true
      label.setLocation(slotX, slotY)
      label.getParent.setComponentZOrder(label, 0)

      slotCardId = Some(defn.id)
      hoveredButtonId = None
      slotHovered = true // <-- cursor is presumably still over it right after the click
      refreshTextPreview()
      refreshSlotIcon()
      label.getParent.repaint()
    }

    def returnToOriginalPosition(): Unit = {
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

  // ----- Actions -----

  private def startGame(): Unit = {
    onNewGame()
  }

  private def quitGame(): Unit = {
    onQuit()
  }
}