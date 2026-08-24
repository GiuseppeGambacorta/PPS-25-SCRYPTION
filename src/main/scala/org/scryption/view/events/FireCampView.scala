package org.scryption.view.events

import org.scryption.view.*
import org.scryption.view.common.GUIAssets.CardViewAssets
import org.scryption.view.common.{CardView, CardViewInfo, ResourceLoader, StatBonus, ZOrder}


import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel, SwingUtilities}
import scala.swing.{FlowPanel, Panel, Swing}

abstract class FireCampView(
                          viewModel: ViewModelDeckEvent,
                          cardWidth: Int,
                          bonus: StatBonus,
                          slotBgImagePath: String
                        ) extends FlowPanel {

  private val setup = CardView.forWidth(cardWidth)
  private val assets = CardViewAssets()
  
  viewModel.ListenForCardsFromTheModel(renderHand)

  private val cardGap = 100
  private val handTopOffset = 500
  private val hoverLiftAmount = 50
  private val slotY = 100

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")
  private val slotBgImage: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(assets.slotPath(slotBgImagePath)).map(new ImageIcon(_))
  private val slotFireImage: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage(assets.slotPath("campfire_f1")).map(new ImageIcon(_))

  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var slotCardIndex: Int = -1

  opaque = false


  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))

    val slotW = setup.geo.cardWidth
    val slotH = setup.geo.cardHeight
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



  private class CardSlot(val index: Int, info: CardViewInfo, val baseX: Int, val baseY: Int) {

    private val frontIcon: ImageIcon =
      setup.render(info, assets.frontTemplatePath(info.cardType)).get

    var isHovered: Boolean = false
    var isInSlot: Boolean = false
    private var isAnimating: Boolean = false

    private var currentX: Int = baseX
    private var currentY: Int = baseY

    val label: JLabel = new JLabel(frontIcon) {
      setBounds(baseX, baseY, setup.geo.cardWidth, setup.geo.cardHeight)
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

    private def handleMouseEnter(): Unit =
      if (!isHovered && !isInSlot && !isAnimating) {
        isHovered = true
        currentY = baseY - hoverLiftAmount
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }

    private def handleMouseExit(): Unit =
      if (isHovered && !isInSlot && !isAnimating) {
        isHovered = false
        currentY = baseY
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }

    private def handleCardClick(): Unit = {
      if (isAnimating) return

      if (isInSlot) {
        isAnimating = true
        isHovered = false

        val newInfo = bonus.apply(info)
        val newIcon: ImageIcon =
          setup.render(newInfo, assets.frontTemplatePath(newInfo.cardType)).get
        label.setIcon(newIcon)

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
        val slotX = (parent.getWidth - setup.geo.cardWidth) / 2
        isInSlot = true
        currentX = slotX
        currentY = FireCampView.this.slotY
        label.setLocation(currentX, currentY)
      }
      slotCardIndex = index
      refreshZOrder()
    }

    private[FireCampView] def returnToHand(): Unit = {
      if (isAnimating) return
      isInSlot = false
      currentX = baseX
      currentY = baseY
      label.setLocation(currentX, currentY)
      refreshZOrder()
    }

    private def sendCardToGameModel(): Unit = {
        viewModel.sendCardToModel(index)
    }

    private[FireCampView] def moveToSlotCoords(): Unit = {
      if (isAnimating) return
      val parent = label.getParent
      if (parent != null) {
        val slotX = (parent.getWidth - setup.geo.cardWidth) / 2
        isInSlot = true
        currentX = slotX
        currentY = FireCampView.this.slotY
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

    val totalWidth = (cardsViewInfo.length - 1) * cardGap + setup.geo.cardWidth

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(Math.max(totalWidth, size.width), handTopOffset + setup.geo.cardHeight + 100)
    }

    val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
      val slot = new CardSlot(i, info, i * cardGap, handTopOffset)
      panel.peer.add(slot.label)
      slot
    }.toVector

    cardSlots = slots
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
      (s: CardSlot) => List(s.label)
    )
}