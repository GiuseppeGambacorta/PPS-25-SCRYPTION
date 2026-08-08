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

class MycologistsView(channel: GUIChannelInterface) extends FlowPanel {

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
  private val slotBgImage: Option[ImageIcon] = ResourceLoader.loadTemplateImage("slots/card_slot_duplicatemerge.png").map(new ImageIcon(_))

  private var currentCards: List[Card[?]] = Nil
  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var slotCardIndex: Int = -1

  private val duplicateOffsetX = 20
  private var showSlotBackground: Boolean = true

  // Configuración del Bonus Visual
  private val visualStatBonus = 2

  opaque = false
  listenToChannel()

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))

    if (showSlotBackground) {
      val slotW = geometry.cardWidth * 3
      val slotH = geometry.cardHeight
      val slotX = (size.width - slotW) / 2

      slotBgImage match {
        case Some(icon) => g.drawImage(icon.getImage, slotX, slotY, slotW, slotH, peer)
        case None =>
          g.setColor(new Color(255, 50, 50, 100))
          g.fillRoundRect(slotX, slotY, slotW, slotH, 20, 20)
      }
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

    // Label para el texto flotante de "+ATK"
    private var bonusLabel: Option[JLabel] = None

    val label: JLabel = new JLabel(frontIcon) {
      setBounds(baseX, baseY, geometry.cardWidth, geometry.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
    }

    val label2: JLabel = new JLabel(frontIcon) {
      setBounds(baseX + duplicateOffsetX, baseY, geometry.cardWidth, geometry.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
    }

    private val pairListener = new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit = handleCardClick()

      override def mousePressed(e: MouseEvent): Unit = {}

      override def mouseReleased(e: MouseEvent): Unit = {}

      override def mouseEntered(e: MouseEvent): Unit = handleMouseEnter()

      override def mouseExited(e: MouseEvent): Unit = handleMouseExit()
    }

    label.addMouseListener(pairListener)
    label2.addMouseListener(pairListener)

    private def handleMouseEnter(): Unit = {
      if (!isHovered && !isInSlot && !isAnimating) {
        isHovered = true
        currentY = baseY - hoverLiftAmount
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + duplicateOffsetX, currentY)
        refreshZOrder()
        label.repaint()
      }
    }

    private def handleMouseExit(): Unit = {
      if (isHovered && !isInSlot && !isAnimating) {
        isHovered = false
        currentY = baseY
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + duplicateOffsetX, currentY)
        refreshZOrder()
        label.repaint()
      }
    }

    private def handleCardClick(): Unit = {
      if (isAnimating) return

      if (isInSlot) {
        isAnimating = true
        isHovered = false

        val mergedInfo = info.copy(
          attack = (info.attack.toInt * visualStatBonus).toString,
          health = (info.health.toInt * visualStatBonus).toString
        )
        val mergedIcon: ImageIcon =
          renderer.render(mergedInfo, CardViewAssets.frontTemplatePath(mergedInfo.cardType)).getOrElse(placeholderIcon)

        val parent = label.getParent
        if (parent != null) {
          val mergedX = (parent.getWidth - geometry.cardWidth) / 2
          label.setLocation(mergedX, currentY)
        }
        label.setIcon(mergedIcon)
        label2.setVisible(false)

        MycologistsView.this.showSlotBackground = false
        MycologistsView.this.repaint()
        label.repaint()

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
        val slotX = (parent.getWidth - geometry.cardWidth * 2) / 2
        this.isInSlot = true
        this.currentX = slotX
        this.currentY = MycologistsView.this.slotY
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + geometry.cardWidth, currentY)
      }
      slotCardIndex = index
      refreshZOrder()
    }

    private[MycologistsView] def moveToSlotCoords(): Unit = {
      if (isAnimating) return
      val parent = label.getParent
      if (parent != null) {
        val slotX = (parent.getWidth - geometry.cardWidth * 2) / 2
        this.isInSlot = true
        this.currentX = slotX
        this.currentY = MycologistsView.this.slotY
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + geometry.cardWidth, currentY)
        refreshZOrder()
      }
    }

    private[MycologistsView] def returnToHand(): Unit = {
      if (isAnimating) return
      this.isInSlot = false
      this.currentX = baseX
      this.currentY = baseY
      label.setLocation(currentX, currentY)
      label2.setLocation(currentX + duplicateOffsetX, currentY)
      refreshZOrder()
    }

    private def sendCardToGameModel(): Unit = {
      if (index >= 0 && index < currentCards.length) {
        channel.sendToGame(GUIMessages.SingleCard(currentCards(index)))
      }
      // Opcional: Limpiar toda la vista o esperar la siguiente escena del juego
      // Swing.onEDT { contents.clear(); peer.repaint() }
    }
  }

  private def renderHand(cardsViewInfo: List[CardViewInfo]): Unit = {
    contents.clear()
    cardSlots = Vector.empty

    if (cardsViewInfo.isEmpty) return

    val handWidth = (cardsViewInfo.length - 1) * cardGap + geometry.cardWidth + duplicateOffsetX
    val slotWidth = geometry.cardWidth * 2
    val totalWidth = math.max(handWidth, slotWidth)

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(Math.max(totalWidth, size.width), handTopOffset + geometry.cardHeight + 100)
    }

    val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
      val posX = i * cardGap
      val posY = handTopOffset
      val slot = new CardSlot(i, info, posX, posY)
      panel.peer.add(slot.label2)
      panel.peer.add(slot.label)
      slot
    }.toVector

    cardSlots = slots
    showSlotBackground = true
    refreshZOrder()

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

    val inSlot = cardSlots.find(_.isInSlot)
    val hovered = cardSlots.find(s => s.isHovered && !s.isInSlot)

    val base = cardSlots
      .filterNot(s => s.isInSlot || hovered.contains(s))
      .sortBy(_.index)

    val backToFront = base ++ hovered.toList ++ inSlot.toList
    val labelsBackToFront = backToFront.flatMap(s => List(s.label, s.label2))

    labelsBackToFront.reverse.zipWithIndex.foreach { case (label, z) =>
      parent.setComponentZOrder(label, z)
    }
  }
}
