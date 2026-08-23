package org.scryption.view.events

import org.scryption.view.common.{CardView, CardViewInfo, GUIAssets, ResourceLoader}
import org.scryption.view.ViewModelDeckEvent
import java.awt.Cursor
import java.awt.event.{MouseEvent, MouseListener}
import javax.swing.{ImageIcon, JLabel, Timer}

import scala.swing.*

class CardSelectionView(viewModel: ViewModelDeckEvent) extends FlowPanel {

  viewModel.ListenForCardsFromTheModel(showCards)

  private val setup = CardView.forWidth(250)
  val assets: GUIAssets.CardViewAssets = setup.assets

  private val backIcon: ImageIcon =
    setup.render(CardViewInfo("", "", "", ""), assets.backTemplatePath).get

  private val cardGap = 30
  private val topOffset = 300
  private val slideStep = 60
  private val tickMillis = 16
  private val slideDistance = 800

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")

  opaque = false

  override protected def paintComponent(g: Graphics2D): Unit = {
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))
    super.paintComponent(g)
  }

  private class CardSlot(val index: Int, info: CardViewInfo, posX: Int) {
    val frontIcon: ImageIcon =
      setup.render(info, assets.frontTemplatePath(info.cardType)).get

    var flipped: Boolean = false
    var selected: Boolean = false
    var siblings: Vector[CardSlot] = Vector()

    val label: JLabel = new JLabel(backIcon) {
      setBounds(posX, topOffset, setup.geo.cardWidth, setup.geo.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)

      addMouseListener(new MouseListener {
        override def mouseClicked(e: MouseEvent): Unit = handleCardClick()
        override def mousePressed(e: MouseEvent): Unit = {}
        override def mouseReleased(e: MouseEvent): Unit = {}
        override def mouseEntered(e: MouseEvent): Unit = {}
        override def mouseExited(e: MouseEvent): Unit = {}
      })
    }

    def setSiblings(all: Vector[CardSlot]): Unit = siblings = all

    private def handleCardClick(): Unit = {
      if (selected || !flipped) {
        if (!flipped) flipToFront()
      } else {
        confirmSelection()
      }
    }

    def flipToFront(): Unit = {
      flipped = true
      label.setIcon(frontIcon)
      label.repaint()
    }

    def flipToBack(): Unit = {
      flipped = false
      label.setIcon(backIcon)
      label.repaint()
    }

    def hide(): Unit = label.setVisible(false)

    private def confirmSelection(): Unit = {
      selected = true
      siblings.foreach { slot =>
        if (slot eq this) slideOut(slot, down = true, isChosen = true)
        else {
          if (slot.flipped) slot.flipToBack()
          slideOut(slot, down = false, isChosen = false)
        }
      }
    }

    private def slideOut(slot: CardSlot, down: Boolean, isChosen: Boolean): Unit = {
      var traveled = 0
      val direction = if (down) 1 else -1
      val timer = new Timer(tickMillis, null)

      timer.addActionListener(_ => {
        val bounds = slot.label.getBounds
        slot.label.setLocation(bounds.x, bounds.y + direction * slideStep)
        traveled += slideStep
        slot.label.repaint()

        if (traveled >= slideDistance) {
          timer.stop()
          slot.hide()
          if (isChosen) {
            val finishTimer = new Timer(150, null)
            finishTimer.setRepeats(false)
            finishTimer.addActionListener(_ => finishSelection())
            finishTimer.start()
          }
        }
      })
      timer.start()
    }

    private def finishSelection(): Unit = {
      viewModel.sendCardToModel(index)
    }
  }

  private def showCards(cardsViewInfo: List[CardViewInfo]): Unit = {
    contents.clear()

    val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
      new CardSlot(i, info, i * (setup.geo.cardWidth + cardGap))
    }.toVector

    slots.foreach(_.setSiblings(slots))

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(
        (setup.geo.cardWidth + cardGap) * cardsViewInfo.length - cardGap,
        topOffset + setup.geo.cardHeight + slideDistance
      )
      slots.foreach(s => peer.add(s.label))
    }

    contents += panel
    peer.revalidate()
    peer.repaint()
  }
}
