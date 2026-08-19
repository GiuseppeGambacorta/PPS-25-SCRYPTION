package org.scryption.view.events

import org.scryption.view.common.{CardView, CardViewInfo, GUIAssets, ItemView, ItemViewInfo, ResourceLoader}
import org.scryption.view.ViewModelItemEvent
import java.awt.{Color, Cursor, Font, Graphics2D, RenderingHints}
import java.awt.event.{MouseEvent, MouseListener}
import javax.swing.{ImageIcon, JLabel, SwingUtilities, Timer}
import scala.swing.*

class ItemSelectionView(viewModel: ViewModelItemEvent) extends FlowPanel {

  // Setup per il rendering carte
  private val cardSetup = CardView.forWidth(250)
  private val cardAssets: GUIAssets.CardViewAssets = cardSetup.assets

  private val itemWidth = 190
  private val itemHeight = 240
  private val itemGap = 35
  private val topOffset = 280
  private val slideStep = 55
  private val tickMillis = 16
  private val slideDistance = 800

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")

  opaque = false

  // Avvia l'ascolto di item o fallback su carta singola
  viewModel.listenForEvents(
    items => SwingUtilities.invokeLater(() => showItems(items)),
    cardInfo => SwingUtilities.invokeLater(() => showCardFallback(cardInfo))
  )

  override protected def paintComponent(g: Graphics2D): Unit = {
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))
    super.paintComponent(g)
  }

  // --- SEZIONE 1: Visualizzazione e Scelta Items ---

  private class ItemSlot(val index: Int, info: ItemViewInfo, posX: Int) {
    val icon: ImageIcon = ItemView.render(info, itemWidth, itemHeight)
    var selected: Boolean = false
    var siblings: Vector[ItemSlot] = Vector()

    val label: JLabel = new JLabel(icon) {
      setBounds(posX, topOffset, itemWidth, itemHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)

      addMouseListener(new MouseListener {
        override def mouseClicked(e: MouseEvent): Unit = confirmSelection()
        override def mousePressed(e: MouseEvent): Unit = {}
        override def mouseReleased(e: MouseEvent): Unit = {}
        override def mouseEntered(e: MouseEvent): Unit = {
          if (!selected) setLocation(posX, topOffset - 12)
        }
        override def mouseExited(e: MouseEvent): Unit = {
          if (!selected) setLocation(posX, topOffset)
        }
      })
    }

    def setSiblings(all: Vector[ItemSlot]): Unit = siblings = all

    def hide(): Unit = label.setVisible(false)

    private def confirmSelection(): Unit = {
      if (selected) return
      selected = true

      siblings.foreach { slot =>
        if (slot eq this) slideOut(slot, down = true, isChosen = true)
        else slideOut(slot, down = false, isChosen = false)
      }
    }

    private def slideOut(slot: ItemSlot, down: Boolean, isChosen: Boolean): Unit = {
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
            finishTimer.addActionListener(_ => viewModel.sendItemToModel(index))
            finishTimer.start()
          }
        }
      })
      timer.start()
    }
  }

  private def showItems(itemsViewInfo: List[ItemViewInfo]): Unit = {
    contents.clear()

    if (itemsViewInfo.isEmpty) {
      peer.revalidate()
      peer.repaint()
      return
    }

    val slots: Vector[ItemSlot] = itemsViewInfo.zipWithIndex.map { case (info, i) =>
      new ItemSlot(i, info, i * (itemWidth + itemGap))
    }.toVector

    slots.foreach(_.setSiblings(slots))

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(
        (itemWidth + itemGap) * itemsViewInfo.length - itemGap,
        topOffset + itemHeight + slideDistance
      )
      slots.foreach(s => peer.add(s.label))
    }

    contents += panel
    peer.revalidate()
    peer.repaint()
  }

  // --- SEZIONE 2: Fallback Carta Singola (Inventario Pieno) ---

  private def showCardFallback(info: CardViewInfo): Unit = {
    contents.clear()

    val frontIcon: ImageIcon =
      cardSetup.render(info, cardAssets.frontTemplatePath(info.cardType)).get

    var selected = false

    // Label di avviso: Inventario Pieno
    val warningLabel = new JLabel("INVENTORY FULL: TAKE A RANDOM CARD INSTEAD") {
      setBounds(0, 180, 500, 45)
      setHorizontalAlignment(javax.swing.SwingConstants.CENTER)
      setFont(new Font("Monospaced", Font.BOLD, 18))
      setForeground(new Color(220, 80, 70))
    }

    val cardLabel = new JLabel(frontIcon) {
      setBounds(125, topOffset, cardSetup.geo.cardWidth, cardSetup.geo.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
      setFocusable(false)
    }

    cardLabel.addMouseListener(new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit = {
        if (!selected) {
          selected = true
          var traveled = 0
          val timer = new Timer(tickMillis, null)

          timer.addActionListener(_ => {
            val bounds = cardLabel.getBounds
            cardLabel.setLocation(bounds.x, bounds.y + slideStep)
            traveled += slideStep
            cardLabel.repaint()

            if (traveled >= slideDistance) {
              timer.stop()
              cardLabel.setVisible(false)
              val finishTimer = new Timer(150, null)
              finishTimer.setRepeats(false)
              finishTimer.addActionListener(_ => viewModel.sendCardToModel())
              finishTimer.start()
            }
          })
          timer.start()
        }
      }
      override def mousePressed(e: MouseEvent): Unit = {}
      override def mouseReleased(e: MouseEvent): Unit = {}
      override def mouseEntered(e: MouseEvent): Unit = {
        if (!selected) cardLabel.setLocation(125, topOffset - 10)
      }
      override def mouseExited(e: MouseEvent): Unit = {
        if (!selected) cardLabel.setLocation(125, topOffset)
      }
    })

    val panel = new Panel {
      peer.setLayout(null)
      opaque = false
      preferredSize = new Dimension(500, topOffset + cardSetup.geo.cardHeight + slideDistance)
      peer.add(warningLabel)
      peer.add(cardLabel)
    }

    contents += panel
    peer.revalidate()
    peer.repaint()
  }
}