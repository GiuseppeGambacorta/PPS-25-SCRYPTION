package org.scryption.view.events

import org.scryption.game.model.Card
import org.scryption.view.*
import org.scryption.{GUIChannelInterface, GUIMessages}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Cursor, Dimension, Graphics2D}
import javax.swing.{ImageIcon, JLabel, SwingUtilities}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.{FlowPanel, Panel, Swing}

/** Strange Stones event: sacrifice one card to upgrade another. The upgraded card gains
 *  the sacrificed card's seals (both its default seals and any it had already picked up)
 *  as added seals.
 *
 *  Two independent slots instead of one, so this doesn't extend [[EventView]]:
 *   - clicking a hand card sends it to the sacrifice slot if that's empty, otherwise to
 *     the upgrade slot if that's empty, otherwise nothing happens (a slot has to be
 *     freed first);
 *   - clicking a card already sitting in either slot returns it to the hand;
 *   - a confirm icon appears once both slots are filled. Clicking it deletes the
 *     sacrificed card, applies its seals to the upgraded card, and sends the result to
 *     the game.
 *
 *  Placeholder asset paths below ("slots/sacrifice.png", "slots/upgrade.png",
 *  "slots/confirm_sacrifice.png") — swap in the real filenames whenever they exist;
 *  missing files just fall back to a colored rectangle like every other slot view.
 *
 *  ASSUMPTION: sending the result needs a message carrying *two* cards, which none of
 *  the existing `GUIMessages` cases do (they only ever send one `SingleCard`). This
 *  assumes a `GUIMessages.Sacrifice(sacrificed, upgraded)` case — add it next to
 *  `Cards`/`SingleCard`/`End` if it doesn't exist yet, or update the one call in
 *  `sendResultToGameModel` below to match whatever you add instead.
 */
class StrangeStonesView(channel: GUIChannelInterface) extends FlowPanel {

  private val setup = CardView.forWidth(size.width)
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

  private var currentCards: List[Card[?]] = Nil
  private var cardSlots: Vector[CardSlot] = Vector.empty
  private var sacrificeIndex: Int = -1
  private var upgradeIndex: Int = -1
  private var confirmLabel: Option[JLabel] = None

  opaque = false
  listenToChannel()

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

  private def listenToChannel(): Unit = {
    Future {
      while (true) {
        channel.receiveFromGame match {
          case GUIMessages.Cards(cards) =>
            Swing.onEDT {
              currentCards = cards
              if (sacrificeIndex != -1 && cards.length <= sacrificeIndex) sacrificeIndex = -1
              if (upgradeIndex != -1 && cards.length <= upgradeIndex) upgradeIndex = -1
              renderHand(currentCards.map(_.toViewInfo))
            }
          case _ =>
        }
      }
    }
  }

  private sealed trait SlotKind
  private object SlotKind {
    case object Hand extends SlotKind
    case object Sacrifice extends SlotKind
    case object Upgrade extends SlotKind
  }

  private class CardSlot(val index: Int, val info: CardViewInfo, val baseX: Int, val baseY: Int) {

    private val baseIcon: ImageIcon =
      renderer.render(info, CardViewAssets.frontTemplatePath(info.cardType)).get

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
        case SlotKind.Sacrifice => returnToHand()
        case SlotKind.Upgrade   => returnToHand()
      }
    }

    private def moveToFirstOpenSlot(): Unit = {
      if (sacrificeIndex == -1) moveTo(SlotKind.Sacrifice)
      else if (upgradeIndex == -1) moveTo(SlotKind.Upgrade)
      // both full: clicking a hand card does nothing until a slot is freed
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

      if (target == SlotKind.Sacrifice) sacrificeIndex = index else upgradeIndex = index
      refreshZOrder()
      refreshConfirmVisibility()
    }

    private[StrangeStonesView] def returnToHand(): Unit = {
      if (isAnimating) return
      if (kind == SlotKind.Sacrifice) sacrificeIndex = -1
      if (kind == SlotKind.Upgrade) upgradeIndex = -1
      kind = SlotKind.Hand
      currentX = baseX
      currentY = baseY
      label.setLocation(currentX, currentY)
      refreshZOrder()
      refreshConfirmVisibility()
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
        renderer.render(mergedInfo, CardViewAssets.frontTemplatePath(mergedInfo.cardType)).get
      label.setIcon(mergedIcon)
      label.repaint()
    }

    private[StrangeStonesView] def hide(): Unit = label.setVisible(false)
  }

  private def confirmSacrifice(): Unit = {
    if (sacrificeIndex == -1 || upgradeIndex == -1) return

    (cardSlots.find(_.index == sacrificeIndex), cardSlots.find(_.index == upgradeIndex)) match {
      case (Some(sacrifice), Some(upgrade)) =>
        sacrifice.isAnimating = true
        upgrade.isAnimating = true

        val mergedInfo = upgrade.info.copy(
          addedSigils = upgrade.info.addedSigils ++ sacrifice.info.defaultSigils ++ sacrifice.info.addedSigils
        )
        upgrade.applyMergedIcon(mergedInfo)
        sacrifice.hide()
        confirmLabel.foreach(_.setVisible(false))

        SwingUtilities.invokeLater(() => {
          Thread.sleep(800)
          Swing.onEDT { sendResultToGameModel(sacrifice.index, upgrade.index) }
        })
      case _ => ()
    }
  }

  private def sendResultToGameModel(sacrificeCardIndex: Int, upgradeCardIndex: Int): Unit = {
    if (
      sacrificeCardIndex >= 0 && sacrificeCardIndex < currentCards.length &&
        upgradeCardIndex >= 0 && upgradeCardIndex < currentCards.length
    ) {
      val sacrificedCard = currentCards(sacrificeCardIndex)
      val upgradedCard = currentCards(upgradeCardIndex)
      channel.sendToGame(GUIMessages.SingleCard(sacrificedCard))
      channel.sendToGame(GUIMessages.SingleCard(upgradedCard))
    }
    // Opcional: Limpiar toda la vista o esperar la siguiente escena del juego
  }

  private def renderHand(cardsViewInfo: List[CardViewInfo]): Unit = {
    contents.clear()
    cardSlots = Vector.empty
    confirmLabel = None

    if (cardsViewInfo.isEmpty) return

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

    if (sacrificeIndex != -1 && sacrificeIndex < slots.length) slots(sacrificeIndex).moveToSlotCoords(SlotKind.Sacrifice)
    if (upgradeIndex != -1 && upgradeIndex < slots.length) slots(upgradeIndex).moveToSlotCoords(SlotKind.Upgrade)

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