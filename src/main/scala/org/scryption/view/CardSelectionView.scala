package org.scryption.view

import org.scryption.view.CardViewAssets
import org.scryption.view.CardViewInfo
import org.scryption.view.ResourceLoader
import org.scryption.GUIChannelInterface

import java.awt.{Cursor, Dimension}
import java.awt.image.BufferedImage
import java.awt.event.{MouseEvent, MouseListener}
import javax.swing.{ImageIcon, JFrame, JLabel, Timer}
import scala.swing.{FlowPanel, Graphics2D, MainFrame, Panel, Swing}

class CardSelectionView(
                         channel: GUIChannelInterface,
                         onSelectionConfirmed: Int => Unit
                       ) extends MainFrame {

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val geometry = CardGeometry(cardWidth = 380)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  private val placeholderIcon: ImageIcon =
    new ImageIcon(new BufferedImage(geometry.cardWidth, geometry.cardHeight, BufferedImage.TYPE_INT_RGB))

  private val backIcon: ImageIcon =
    renderer.render(CardViewInfo("", "", "", ""), CardViewAssets.backTemplatePath).getOrElse(placeholderIcon)

  private val cardGap = 30
  private val topOffset = 400
  private val slideStep = 60
  private val tickMillis = 16
  private val slideDistance = 800

  private var backgroundPanel: BackgroundPanel = _

  title = "Selección de Carta"
  peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)

  setupBackground()

  private def setupBackground(): Unit = {
    backgroundPanel = new BackgroundPanel()
    contents = backgroundPanel
  }

  private class BackgroundPanel extends FlowPanel {
    opaque = false
    private val backgroundImage: Option[BufferedImage] = ResourceLoader.loadTemplateImage("table.png")
    override protected def paintComponent(g: Graphics2D): Unit = {
      backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))
      super.paintComponent(g)
    }
  }

  private class CardSlot(val index: Int, info: CardViewInfo, posX: Int) {
    val frontIcon: ImageIcon =
      renderer.render(info, CardViewAssets.frontTemplatePath(info.cardType)).getOrElse(placeholderIcon)

    var flipped: Boolean = false
    var selected: Boolean = false
    var siblings: Vector[CardSlot] = Vector()

    val label: JLabel = new JLabel(backIcon) {
      setBounds(posX, topOffset, geometry.cardWidth, geometry.cardHeight)
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
      onSelectionConfirmed(index)

      Swing.onEDT {
        peer.setVisible(false)
        peer.dispose()
      }
    }
  }

  def showCards(cardsViewInfo: List[CardViewInfo]): Unit = {
    Swing.onEDT {
      //backgroundPanel.contents = Nil

      val slots: Vector[CardSlot] = cardsViewInfo.zipWithIndex.map { case (info, i) =>
        new CardSlot(i, info, i * (geometry.cardWidth + cardGap))
      }.toVector

      slots.foreach(_.setSiblings(slots))

      val panel = new Panel {
        peer.setLayout(null)
        opaque = false
        preferredSize = new Dimension(
          (geometry.cardWidth + cardGap) * cardsViewInfo.length - cardGap,
          topOffset + geometry.cardHeight + slideDistance
        )
        slots.foreach(s => peer.add(s.label))
      }

      backgroundPanel.contents += panel
      backgroundPanel.peer.revalidate()
      backgroundPanel.peer.repaint()

      if (!peer.isVisible()) peer.setVisible(true)
    }
  }

  override def close(): Unit = peer.dispose()
}