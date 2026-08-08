package org.scryption.view

import org.scryption.game.model.Card
import org.scryption.view.{CardViewAssets, CardViewInfo, ResourceLoader}
import org.scryption.{GUIChannelInterface, GUIMessages}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Cursor, Dimension, Font, Graphics2D}
import javax.swing.{ImageIcon, JLabel, SwingUtilities, Timer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.{FlowPanel, Panel, Swing}

class FireCampHealthView(channel: GUIChannelInterface) extends FlowPanel {

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val geometry = CardGeometry(cardWidth = 380)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  private val placeholderIcon: ImageIcon =
    new ImageIcon(new BufferedImage(geometry.cardWidth, geometry.cardHeight, BufferedImage.TYPE_INT_RGB))

  private val cardGap = 100
  private val handTopOffset = 800
  private val hoverLiftAmount = 50
  private val slotY = 100

  private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")
  private val slotBgImage: Option[ImageIcon] = ResourceLoader.loadTemplateImage("slots/slot_statboost_health.png").map(new ImageIcon(_))
  private val slotFireImage: Option[ImageIcon] = ResourceLoader.loadTemplateImage("slots/slot_campfire_f1.png").map(new ImageIcon(_))

  private var currentCards: List[Card[?]] = Nil
  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var slotCardIndex: Int = -1

  // Configuración del Bonus Visual
  private val visualHealthBonus = 2

  opaque = false
  listenToChannel()

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))

    val slotW = geometry.cardWidth
    val slotH = geometry.cardHeight
    val slotX = (size.width - slotW) / 2

    slotBgImage match {
      case Some(icon) => g.drawImage(icon.getImage, slotX, slotY, slotW, slotH, peer)
      case None =>
        g.setColor(new Color(255, 50, 50, 100))
        g.fillRoundRect(slotX, slotY, slotW, slotH, 20, 20)
    }
    slotFireImage match {
      case Some(icon) => g.drawImage(icon.getImage, slotX, slotY, slotW, slotH, peer)
      case None =>
        g.setColor(new Color(255, 100, 0, 150))
        g.fillRoundRect(slotX + 10, slotY + 10, slotW - 20, slotH - 20, 15, 15)
    }
  }

  private def listenToChannel(): Unit = {
    Future {
      while (true) {
        val msg = channel.receiveFromGame
        msg match {
          case GUIMessages.Cards(cards) =>
            Swing.onEDT {
              this.currentCards = cards
              if (slotCardIndex != -1 && cards.length <= slotCardIndex) slotCardIndex = -1
              renderHand(currentCards.map(_.toViewInfo))
            }
          case _ =>
        }
      }
    }
  }

  private class CardSlot(val index: Int, info: CardViewInfo, val baseX: Int, val baseY: Int) {

    val frontIcon: ImageIcon =
      renderer.render(info, CardViewAssets.frontTemplatePath(info.cardType)).getOrElse(placeholderIcon)

    var isHovered: Boolean = false
    var isInSlot: Boolean = false
    var isAnimating: Boolean = false

    var currentX: Int = baseX
    var currentY: Int = baseY

    private var bonusLabel: Option[JLabel] = None

    val label: JLabel = new JLabel(frontIcon) {
      setBounds(baseX, baseY, geometry.cardWidth, geometry.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
    }

    label.addMouseListener(new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit = handleCardClick()
      override def mousePressed(e: MouseEvent): Unit = {}
      override def mouseReleased(e: MouseEvent): Unit = {}
      override def mouseEntered(e: MouseEvent): Unit = handleMouseEnter()
      override def mouseExited(e: MouseEvent): Unit = handleMouseExit()
    })

    private def handleMouseEnter(): Unit = {
      if (!isHovered && !isInSlot && !isAnimating) {
        isHovered = true
        currentY = baseY - hoverLiftAmount
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }
    }

    private def handleMouseExit(): Unit = {
      if (isHovered && !isInSlot && !isAnimating) {
        isHovered = false
        currentY = baseY
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }
    }

    private def handleCardClick(): Unit = {
      if (isAnimating) return

      if (isInSlot) {
        isAnimating = true
        isHovered = false

        val newInfo = info.copy(health = (info.health.toInt + visualHealthBonus).toString)
        val newIcon: ImageIcon =
          renderer.render(newInfo, CardViewAssets.frontTemplatePath(newInfo.cardType)).getOrElse(placeholderIcon)
        label.setIcon(newIcon)

        SwingUtilities.invokeLater(() => {
          Thread.sleep(800)
          Swing.onEDT {
            sendCardToGameModel()
          }
        })
      } else {
        moveToSlot()
      }
    }

    private def moveToSlot(): Unit = {
      val previousSlotIndex = slotCardIndex
      if (previousSlotIndex != -1 && previousSlotIndex < cardSlots.length) {
        cardSlots(previousSlotIndex).returnToHand()
      }

      val parent = label.getParent
      if (parent != null) {
        val slotX = (parent.getWidth - geometry.cardWidth) / 2
        this.isInSlot = true
        this.currentX = slotX
        this.currentY = FireCampHealthView.this.slotY
        label.setLocation(currentX, currentY)
      }
      slotCardIndex = index
      refreshZOrder()
    }

    private[FireCampHealthView] def returnToHand(): Unit = {
      if (isAnimating) return
      this.isInSlot = false
      this.currentX = baseX
      this.currentY = baseY
      label.setLocation(currentX, currentY)
      refreshZOrder()
    }

    private def sendCardToGameModel(): Unit = {
      if (index >= 0 && index < currentCards.length) {
        channel.sendToGame(GUIMessages.SingleCard(currentCards(index)))
      }
      // Opcional: Limpiar toda la vista o esperar la siguiente escena del juego
      // Swing.onEDT { contents.clear(); peer.repaint() }
    }

    private[FireCampHealthView] def moveToSlotCoords(): Unit = {
      if (isAnimating) return
      val parent = label.getParent
      if (parent != null) {
        val slotX = (parent.getWidth - geometry.cardWidth) / 2
        val slotYTarget = FireCampHealthView.this.slotY
        this.isInSlot = true
        this.currentX = slotX
        this.currentY = slotYTarget
        label.setLocation(currentX, currentY)
        parent.setComponentZOrder(label, 0)
        parent.repaint()
      }
    }
  }

  private def renderHand(cardsViewInfo: List[CardViewInfo]): Unit = {
    contents.clear()
    cardSlots = Vector.empty

    if (cardsViewInfo.isEmpty) return

    val totalWidth = (cardsViewInfo.length - 1) * cardGap + geometry.cardWidth

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(Math.max(totalWidth, size.width), handTopOffset + geometry.cardHeight + 100)
    }

    val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
      val posX = i * cardGap
      val posY = handTopOffset
      val slot = new CardSlot(i, info, posX, posY)
      panel.peer.add(slot.label)
      slot
    }.toVector

    cardSlots = slots
    refreshZOrder() // <-- new: sets initial back-to-front order

    if (slotCardIndex != -1 && slotCardIndex < slots.length) {
      slots(slotCardIndex).moveToSlotCoords()
    }

    contents += panel
    peer.revalidate()
    peer.repaint()
  }

  private def refreshZOrder(): Unit = {
    if (cardSlots.isEmpty) return
    val parent = cardSlots.head.label.getParent
    if (parent == null) return

    // Back-to-front: base cards in natural index order, then the hovered
    // card on top of those, then the slotted card always frontmost of all.
    val inSlot = cardSlots.find(_.isInSlot)
    val hovered = cardSlots.find(s => s.isHovered && !s.isInSlot)

    val base = cardSlots
      .filterNot(s => s.isInSlot || hovered.contains(s))
      .sortBy(_.index)

    val backToFront = base ++ hovered.toList ++ inSlot.toList

    // setComponentZOrder: 0 = frontmost, so assign in reverse
    backToFront.reverse.zipWithIndex.foreach { case (slot, z) =>
      parent.setComponentZOrder(slot.label, z)
    }
  }
}
