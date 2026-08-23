package org.scryption.view.events

import org.scryption.Trial
import org.scryption.view.ViewModelDeckEvent
import org.scryption.view.common.{CardView, CardViewInfo, GUIAssets, ResourceLoader}

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Cursor, Font}
import javax.swing.{ImageIcon, JButton, JLabel, SwingConstants, Timer}
import scala.swing.*

class TrialView(viewModel: ViewModelDeckEvent) extends FlowPanel {

  viewModel.ListenForCardsFromTheModel(showCards)

  private val setup = CardView.forWidth(220)
  val assets: GUIAssets.CardViewAssets = setup.assets

  private val backIcon: ImageIcon =
    setup.render(CardViewInfo("", "", "", ""), assets.backTemplatePath).get

  private val cardGap = 15
  private val topOffset = 130
  private val slideStep = 60
  private val tickMillis = 16
  private val slideDistance = 800

  private val backgroundImage = ResourceLoader.loadTemplateImage("table.png")

  opaque = false

  override protected def paintComponent(g: Graphics2D): Unit = {
    backgroundImage.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, peer))
    super.paintComponent(g)
  }

  private class CardSlot(val index: Int, info: CardViewInfo, posX: Int, isRewardMode: Boolean = false) {
    val frontIcon: ImageIcon =
      setup.render(info, assets.frontTemplatePath(info.cardType)).get

    var flipped: Boolean = false
    var selected: Boolean = false
    var siblings: Vector[CardSlot] = Vector()

    val label: JLabel = new JLabel(if (isRewardMode) frontIcon else backIcon) {
      setBounds(posX, topOffset, setup.geo.cardWidth, setup.geo.cardHeight)
      setOpaque(false)
      setCursor(new Cursor(if (isRewardMode) Cursor.HAND_CURSOR else Cursor.DEFAULT_CURSOR))
      setFocusable(false)

      addMouseListener(new MouseListener {
        override def mouseClicked(e: MouseEvent): Unit = handleCardClick()
        override def mousePressed(e: MouseEvent): Unit = {}
        override def mouseReleased(e: MouseEvent): Unit = {}
        override def mouseEntered(e: MouseEvent): Unit = {}
        override def mouseExited(e: MouseEvent): Unit = {}
      })
    }

    if (isRewardMode) flipped = true

    def setSiblings(all: Vector[CardSlot]): Unit = siblings = all

    private def handleCardClick(): Unit = {
      if (isRewardMode && !selected) {
        confirmReward()
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

    private def confirmReward(): Unit = {
      selected = true
      slideOut(this, down = true, isChosen = true)
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
            finishTimer.addActionListener(_ => viewModel.sendCardToModel(index))
            finishTimer.start()
          }
        }
      })
      timer.start()
    }
  }

  private def showCards(cardsViewInfo: List[CardViewInfo]): Unit = {
    contents.clear()

    cardsViewInfo match {
      case Nil =>
        // Trial failed or event finished
        peer.revalidate()
        peer.repaint()

      case singleCard :: Nil =>
        val totalWidth = 900
        val cardX = (totalWidth - setup.geo.cardWidth) / 2
        val rewardSlot = new CardSlot(0, singleCard, cardX, isRewardMode = true)

        val headerLabel = new JLabel("Trial Passed! Click the card to claim it") {
          setBounds(0, 50, totalWidth, 45)
          setFont(new Font("SansSerif", Font.BOLD, 26))
          setForeground(new Color(255, 235, 130))
          setHorizontalAlignment(SwingConstants.CENTER)
        }

        val panel = new Panel {
          peer.setLayout(null)
          opaque = false
          preferredSize = new Dimension(totalWidth, topOffset + setup.geo.cardHeight + slideDistance)
          peer.add(headerLabel)
          peer.add(rewardSlot.label)
        }

        contents += panel
        peer.revalidate()
        peer.repaint()

      case multipleCards =>
        val cardsCount = multipleCards.length
        val totalCardsWidth = (setup.geo.cardWidth * cardsCount) + (cardGap * (cardsCount - 1))
        val panelWidth = Math.max(totalCardsWidth + 80, 850)
        val startX = (panelWidth - totalCardsWidth) / 2

        val slots: Vector[CardSlot] = multipleCards.zipWithIndex.map { case (info, i) =>
          new CardSlot(i, info, startX + i * (setup.geo.cardWidth + cardGap))
        }.toVector

        slots.foreach(_.setSiblings(slots))

        val titleLabel = new JLabel("Trial Event: Choose a trial to face") {
          setBounds(0, 40, panelWidth, 40)
          setFont(new Font("SansSerif", Font.BOLD, 26))
          setForeground(Color.WHITE)
          setHorizontalAlignment(SwingConstants.CENTER)
        }

        // Posizionamento bottoni sotto le carte
        val buttonsY = topOffset + setup.geo.cardHeight + 40
        val btnWidth = 220
        val btnHeight = 45
        val btnGap = 20
        val totalButtonsWidth = (btnWidth * 3) + (btnGap * 2)
        val startBtnX = (panelWidth - totalButtonsWidth) / 2

        val btnHealth = new JButton("Trial of Health (HP >= 10)") {
          setBounds(startBtnX, buttonsY, btnWidth, btnHeight)
          setFont(new Font("SansSerif", Font.BOLD, 13))
        }

        val btnAttack = new JButton("Trial of Attack (ATK >= 6)") {
          setBounds(startBtnX + btnWidth + btnGap, buttonsY, btnWidth, btnHeight)
          setFont(new Font("SansSerif", Font.BOLD, 13))
        }

        val btnSeals = new JButton("Trial of Seals (Seals >= 4)") {
          setBounds(startBtnX + (btnWidth + btnGap) * 2, buttonsY, btnWidth, btnHeight)
          setFont(new Font("SansSerif", Font.BOLD, 13))
        }

        val allButtons = List(btnHealth, btnAttack, btnSeals)

        def handleChoice(choice: Trial): Unit = {
          allButtons.foreach(_.setEnabled(false))
          slots.foreach(_.flipToFront())

          val timer = new Timer(1000, null)
          timer.setRepeats(false)
          timer.addActionListener(_ => viewModel.sendTrialChoiceToModel(choice))
          timer.start()
        }

        btnHealth.addActionListener(_ => handleChoice(Trial.Health))
        btnAttack.addActionListener(_ => handleChoice(Trial.Attack))
        btnSeals.addActionListener(_ => handleChoice(Trial.Seals))

        val panel = new Panel {
          peer.setLayout(null)
          opaque = false
          preferredSize = new Dimension(
            panelWidth,
            buttonsY + btnHeight + 60
          )
          peer.add(titleLabel)
          peer.add(btnHealth)
          peer.add(btnAttack)
          peer.add(btnSeals)
          slots.foreach(s => peer.add(s.label))
        }

        contents += panel
        peer.revalidate()
        peer.repaint()
    }
  }
}
