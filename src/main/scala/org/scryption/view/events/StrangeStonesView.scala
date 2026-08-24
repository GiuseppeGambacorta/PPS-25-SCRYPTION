package org.scryption.view.events

import org.scryption.view.*
import org.scryption.view.common.{CardView, CardViewInfo, ResourceLoader}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel, SwingUtilities}

import scala.swing.{FlowPanel, Panel, Swing}

class StrangeStonesView(viewModel: ViewModelDeckEvent) extends FlowPanel {

  private var selectionState: Int = 0

  viewModel.ListenForCardsFromTheModel(renderHand)

  private val setup = CardView.forWidth(250)
  private val assets = setup.assets
  private val geometry = setup.geo
  private val renderer = setup

  private val cardGap = 100
  private val handTopOffset = 800
  private val hoverLiftAmount = 50
  private val slotY = 100
  private val slotGap = 60
  private val confirmSize = 100
  private val confirmGapBelowSlots = 40

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")
  private val sacrificeSlotOverlay: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage("slots/card_slot_sacrifice.png").map(new ImageIcon(_))
  private val upgradeSlotOverlay: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage("slots/card_slot_host.png").map(new ImageIcon(_))
  private val confirmIcon: Option[ImageIcon] =
    ResourceLoader.loadTemplateImage("slots/sacrifice_button.png").map(new ImageIcon(_))

  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var sacrificeIndex: Int = -1
  private var upgradeIndex: Int = -1
  private var confirmLabel: Option[JLabel] = None

  private var lockedSacrificeInfo: Option[CardViewInfo] = None
  private var lockedSacrificeOriginalIndex: Int = -1

  opaque = false


  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))

    val slotW = geometry.cardWidth
    val slotH = geometry.cardHeight
    val pairWidth = slotW * 2 + slotGap
    val sacrificeX = (size.width - pairWidth) / 2
    val upgradeX = sacrificeX + slotW + slotGap

    drawSlotOverlay(g, sacrificeSlotOverlay, sacrificeX, slotY, slotW, slotH, new Color(255, 50, 50, 100))
    drawSlotOverlay(g, upgradeSlotOverlay, upgradeX, slotY, slotW, slotH, new Color(80, 120, 255, 100))
  }

  private def drawSlotOverlay(
                               g: Graphics2D,
                               overlay: Option[ImageIcon],
                               x: Int,
                               y: Int,
                               w: Int,
                               h: Int,
                               fallbackColor: Color
                             ): Unit =
    overlay match {
      case Some(icon) => g.drawImage(icon.getImage, x, y, w, h, peer)
      case None =>
        g.setColor(fallbackColor)
        g.fillRoundRect(x, y, w, h, 20, 20)
    }



  private sealed trait SlotKind
  private object SlotKind {
    case object Hand extends SlotKind
    case object Sacrifice extends SlotKind
    case object Upgrade extends SlotKind
  }

  private class CardSlot(val index: Int, val info: CardViewInfo, val baseX: Int, val baseY: Int) {

    private val baseIcon: ImageIcon =
      renderer.render(info, assets.frontTemplatePath(info.cardType)).get

    var isHovered: Boolean = false
    var isAnimating: Boolean = false
    var kind: SlotKind = SlotKind.Hand

    var currentX: Int = baseX
    var currentY: Int = baseY

    val label: JLabel = new JLabel(baseIcon) {
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

    private def handleMouseEnter(): Unit =
      if (!isHovered && kind == SlotKind.Hand && !isAnimating) {
        isHovered = true
        currentY = baseY - hoverLiftAmount
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }

    private def handleMouseExit(): Unit =
      if (isHovered && kind == SlotKind.Hand && !isAnimating) {
        isHovered = false
        currentY = baseY
        label.setLocation(currentX, currentY)
        refreshZOrder()
        label.repaint()
      }

    private def handleCardClick(): Unit = {
      if (isAnimating) return

      kind match {
        case SlotKind.Hand      => moveToFirstOpenSlot()
        case SlotKind.Sacrifice =>
        case SlotKind.Upgrade   => returnToHand()
      }
    }

    private def moveToFirstOpenSlot(): Unit = {
      if (selectionState == 0) {
        if (sacrificeIndex == -1) moveTo(SlotKind.Sacrifice)
      } else if (selectionState == 1) {
        if (upgradeIndex == -1) moveTo(SlotKind.Upgrade)
      }
    }

    private def moveTo(target: SlotKind): Unit = {
      val parent = label.getParent
      if (parent == null) return

      val slotW = geometry.cardWidth
      val pairWidth = slotW * 2 + slotGap
      val sacrificeX = (parent.getWidth - pairWidth) / 2
      val upgradeX = sacrificeX + slotW + slotGap

      isHovered = false
      kind = target
      currentX = if (target == SlotKind.Sacrifice) sacrificeX else upgradeX
      currentY = slotY
      label.setLocation(currentX, currentY)

      if (target == SlotKind.Sacrifice) {
        sacrificeIndex = index
        selectionState = 1
        lockedSacrificeInfo = Some(info)
        lockedSacrificeOriginalIndex = index
        viewModel.sendCardToModel(index)
        viewModel.ListenForCardsFromTheModel(renderHand)
      } else if (target == SlotKind.Upgrade) {
        upgradeIndex = index
        refreshConfirmVisibility()
      }

      refreshZOrder()
    }

    private[StrangeStonesView] def returnToHand(): Unit = {
      if (isAnimating) return

      if (kind == SlotKind.Upgrade) {
        upgradeIndex = -1
        kind = SlotKind.Hand
        currentX = baseX
        currentY = baseY
        label.setLocation(currentX, currentY)
        refreshZOrder()
        refreshConfirmVisibility()
      }
    }

    private[StrangeStonesView] def moveToSlotCoords(target: SlotKind): Unit = {
      val parent = label.getParent
      if (parent == null) return

      val slotW = geometry.cardWidth
      val pairWidth = slotW * 2 + slotGap
      val sacrificeX = (parent.getWidth - pairWidth) / 2
      val upgradeX = sacrificeX + slotW + slotGap

      kind = target
      currentX = if (target == SlotKind.Sacrifice) sacrificeX else upgradeX
      currentY = slotY
      label.setLocation(currentX, currentY)
    }

    private[StrangeStonesView] def applyMergedIcon(mergedInfo: CardViewInfo): Unit = {
      val mergedIcon =
        renderer.render(mergedInfo, assets.frontTemplatePath(mergedInfo.cardType)).get
      label.setIcon(mergedIcon)
      label.repaint()
    }

    private[StrangeStonesView] def hide(): Unit = label.setVisible(false)
  }

  private def confirmSacrifice(): Unit = {
    if (sacrificeIndex == -1 || upgradeIndex == -1) return

    (cardSlots.find(_.index == upgradeIndex)) match {
      case Some(upgrade) =>

        upgrade.isAnimating = true

        val sacrificeInfo = lockedSacrificeInfo.getOrElse(upgrade.info)

        val mergedInfo = upgrade.info.copy(
          addedSigils = upgrade.info.addedSigils ++ sacrificeInfo.defaultSigils ++ sacrificeInfo.addedSigils
        )
        upgrade.applyMergedIcon(mergedInfo)

        val sacrificeSlot = cardSlots.find(_.index == sacrificeIndex)
        sacrificeSlot.foreach(_.hide())

        confirmLabel.foreach(_.setVisible(false))

        SwingUtilities.invokeLater(() => {
          Thread.sleep(800)
          Swing.onEDT {
            viewModel.sendCardToModel(upgrade.index)
            selectionState = 0
            sacrificeIndex = -1
            upgradeIndex = -1
            lockedSacrificeInfo = None
          }
        })
      case _ => ()
    }
  }

  private def renderHand(cardsViewInfo: List[CardViewInfo]): Unit = {

    val isUpgradingPhase = selectionState == 1

    contents.clear()

    cardSlots = Vector.empty
    confirmLabel = None

    if (cardsViewInfo.isEmpty && !isUpgradingPhase) return

    val handWidth = (cardsViewInfo.length - 1) * cardGap + geometry.cardWidth
    val pairWidth = geometry.cardWidth * 2 + slotGap
    val totalWidth = math.max(handWidth, pairWidth)
    val totalHeight = handTopOffset + geometry.cardHeight + 100

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(Math.max(totalWidth, size.width), totalHeight)
    }

    val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
      val slot = new CardSlot(i, info, i * cardGap, handTopOffset)
      panel.peer.add(slot.label)
      slot
    }.toVector

    cardSlots = slots

    if (isUpgradingPhase && lockedSacrificeInfo.isDefined) {

      val sacrificeInfo = lockedSacrificeInfo.get
      val sacrificeSlot = new CardSlot(lockedSacrificeOriginalIndex, sacrificeInfo, -1, -1) {
        override private[StrangeStonesView] def returnToHand(): Unit = {}
      }

      val slotW = geometry.cardWidth
      val pairWidth = slotW * 2 + slotGap
      val sacrificeX = (panel.preferredSize.width - pairWidth) / 2

      sacrificeSlot.kind = SlotKind.Sacrifice
      sacrificeSlot.currentX = sacrificeX
      sacrificeSlot.currentY = slotY
      sacrificeSlot.label.setLocation(sacrificeX, slotY)
      sacrificeSlot.isAnimating = false

      panel.peer.add(sacrificeSlot.label)

      cardSlots = cardSlots :+ sacrificeSlot
      sacrificeIndex = lockedSacrificeOriginalIndex
    }

    if (upgradeIndex != -1 && upgradeIndex < slots.length) {
      slots(upgradeIndex).moveToSlotCoords(SlotKind.Upgrade)
    }

    val confirm: JLabel = new JLabel(confirmIcon.orNull) {
      setBounds(
        (panel.preferredSize.width - confirmSize) / 2,
        slotY + geometry.cardHeight + confirmGapBelowSlots,
        confirmSize,
        confirmSize
      )
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
      setVisible(sacrificeIndex != -1 && upgradeIndex != -1)
    }
    confirm.addMouseListener(new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit = confirmSacrifice()
      override def mousePressed(e: MouseEvent): Unit = {}
      override def mouseReleased(e: MouseEvent): Unit = {}
      override def mouseEntered(e: MouseEvent): Unit = {}
      override def mouseExited(e: MouseEvent): Unit = {}
    })
    panel.peer.add(confirm)
    confirmLabel = Some(confirm)

    refreshZOrder()

    contents += panel
    peer.revalidate()
    peer.repaint()
  }

  private def refreshConfirmVisibility(): Unit =
    confirmLabel.foreach(_.setVisible(sacrificeIndex != -1 && upgradeIndex != -1))

  private def refreshZOrder(): Unit = {
    if (cardSlots.isEmpty) return
    val parent = cardSlots.head.label.getParent
    if (parent == null) return

    val hovered = cardSlots.find(s => s.isHovered && s.kind == SlotKind.Hand)
    val base = cardSlots.filter(_.kind == SlotKind.Hand).filterNot(hovered.contains).sortBy(_.index)
    val slotted = cardSlots.filter(s => s.kind == SlotKind.Sacrifice || s.kind == SlotKind.Upgrade)

    val backToFront = base ++ hovered.toList ++ slotted
    backToFront.reverse.zipWithIndex.foreach { case (slot, z) => parent.setComponentZOrder(slot.label, z) }
  }
}
