package org.scryption.view.events

import org.scryption.view.*
import org.scryption.view.common.{CardView, CardViewInfo, ResourceLoader, ZOrder}


import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel, SwingUtilities}
import scala.swing.{FlowPanel, Panel, Swing}

/** Mycologists event: pick two matching cards, merge them into one boosted card.
 *
 *  This doesn't fit the [[EventView]] template — the slot holds a *pair* of stacked
 *  labels rather than one, and confirming merges them into a single icon instead of
 *  just re-rendering the same label — so it keeps its own CardSlot. It does reuse the
 *  shared [[CardRendering]] setup and the shared [[ZOrder]] stacking rule, which used
 *  to be copy-pasted here too.
 */
class MycologistsView(viewModel: ViewModelEvent) extends FlowPanel {

 
  viewModel.ListenForCardsFromTheModel(renderHand)

  private val setup = CardView.forWidth(250)
  private val assets = setup.assets
  private val geometry = setup.geo
  private val renderer = setup

  private val cardGap = 100
  private val handTopOffset = 600
  private val hoverLiftAmount = 50
  private val slotY = 100

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")
  private val slotBgImage: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage("slots/card_slot_duplicatemerge.png").map(new ImageIcon(_))

  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var slotCardIndex: Int = -1

  private val duplicateOffsetX = 20
  private var showSlotBackground: Boolean = true

  // Configuración del Bonus Visual
  private val visualStatBonus = 2

  opaque = false


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


  private class CardSlot(val index: Int, info: CardViewInfo, val baseX: Int, val baseY: Int) {

    val frontIcon: ImageIcon =
      renderer.render(info, assets.frontTemplatePath(info.cardType)).get

    var isHovered: Boolean = false
    var isInSlot: Boolean = false
    var isAnimating: Boolean = false

    var currentX: Int = baseX
    var currentY: Int = baseY

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

    private def handleMouseEnter(): Unit =
      if (!isHovered && !isInSlot && !isAnimating) {
        isHovered = true
        currentY = baseY - hoverLiftAmount
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + duplicateOffsetX, currentY)
        refreshZOrder()
        label.repaint()
      }

    private def handleMouseExit(): Unit =
      if (isHovered && !isInSlot && !isAnimating) {
        isHovered = false
        currentY = baseY
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + duplicateOffsetX, currentY)
        refreshZOrder()
        label.repaint()
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
          renderer.render(mergedInfo, assets.frontTemplatePath(mergedInfo.cardType)).get

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
          Swing.onEDT { sendCardToGameModel() }
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
        isInSlot = true
        currentX = slotX
        currentY = MycologistsView.this.slotY
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
        isInSlot = true
        currentX = slotX
        currentY = MycologistsView.this.slotY
        label.setLocation(currentX, currentY)
        label2.setLocation(currentX + geometry.cardWidth, currentY)
        refreshZOrder()
      }
    }

    private[MycologistsView] def returnToHand(): Unit = {
      if (isAnimating) return
      isInSlot = false
      currentX = baseX
      currentY = baseY
      label.setLocation(currentX, currentY)
      label2.setLocation(currentX + duplicateOffsetX, currentY)
      refreshZOrder()
    }

    private def sendCardToGameModel(): Unit = {
       viewModel.sendCardToModel(index)
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
      val slot = new CardSlot(i, info, i * cardGap, handTopOffset)
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

  private def refreshZOrder(): Unit =
    ZOrder(
      cardSlots,
      (s: CardSlot) => s.isInSlot,
      (s: CardSlot) => s.isHovered,
      (s: CardSlot) => s.index,
      (s: CardSlot) => List(s.label, s.label2)
    )
}