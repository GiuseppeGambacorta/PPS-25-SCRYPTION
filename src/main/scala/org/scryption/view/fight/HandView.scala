package org.scryption.view.fight


import org.scryption.game.model.Card
import org.scryption.view.common.cardToViewInfo
import org.scryption.view.common.GUIGeometry.CardGeometry
import org.scryption.view.common.{CardView, CardViewInfo}
import org.scryption.view.common.ResourceLoader
import org.scryption.view.common.GUIAssets.CardViewAssets

import scala.swing.*
import scala.swing.event.{ButtonClicked, MouseClicked, MouseExited, MouseMoved}
import java.awt.{BasicStroke, Color, Cursor, Dimension}
import javax.swing.Timer

class HandView(onCardSelected: Option[Card[?]] => Unit) extends BorderPanel:

  var interactable = true
  opaque = false

  private var selectedPanel: Option[HandCardPanel] = None
  private var blinkState: Boolean = false

  // blinking border animation timer every 400ms
  private val blinkTimer = new Timer(400, _ => {
    blinkState = !blinkState
    cardsContainer.repaint()
  })
  blinkTimer.start()

  private val toggleButton = new Button("▼"):
    cursor = new Cursor(Cursor.HAND_CURSOR)
    background = new Color(50, 50, 55)
    foreground = Color.WHITE
    font = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16)
    preferredSize = new Dimension(80, 30)
    tooltip = "Hide / Show Hand"

  private val buttonWrapper = new FlowPanel:
    opaque = false
    contents += toggleButton

  private val cardsContainer = new BoxPanel(Orientation.Horizontal):
    opaque = true
    background = new Color(30, 30, 35)
    border = Swing.EmptyBorder(15, 15, 15, 15)

  private val scrollPane = new ScrollPane(cardsContainer):
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Never
    preferredSize = new Dimension(0, 320)

  layout(buttonWrapper) = BorderPanel.Position.North
  layout(scrollPane) = BorderPanel.Position.Center

  listenTo(toggleButton)
  reactions += {
    case ButtonClicked(`toggleButton`) =>
      scrollPane.visible = !scrollPane.visible
      toggleButton.text = if scrollPane.visible then "▼" else "▲"
      revalidate()
      repaint()
  }

  private val geometry = CardGeometry(cardWidth = 180)
  private val renderer = new CardView(geometry, new CardViewAssets)

  private class HandCardPanel(val card: Card[?], img: java.awt.Image) extends Panel:
    opaque = false
    private val cardW = 180
    private val cardH: Int = (180 * 1.52).toInt
    private val panelSize = new Dimension(cardW, cardH + 20)
    preferredSize = panelSize
    minimumSize = panelSize
    maximumSize = panelSize
    private var isHovered: Boolean = false

    listenTo(mouse.clicks, mouse.moves)
    reactions += {
      case e: MouseMoved =>
        if interactable then
          val wasHovered = isHovered
          isHovered = e.point.x >= 0 && e.point.x <= cardW && e.point.y >= 0 && e.point.y <= (cardH + 20)
          if isHovered != wasHovered then
            cursor = if isHovered then new Cursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor
            repaint()
      case MouseExited(_, _, _) =>
        isHovered = false
        cursor = Cursor.getDefaultCursor
        repaint()
      case MouseClicked(_, _, _, _, _) =>
        if interactable && isHovered then
          if selectedPanel.contains(this) then
            selectedPanel = None
            onCardSelected(None)
          else
            selectedPanel = Some(this)
            onCardSelected(Some(card))
          cardsContainer.repaint()
    }

    override protected def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      val isSelected = selectedPanel.contains(this)
      val yOffset = if isHovered || isSelected then 0 else 20
      g.drawImage(img, 0, yOffset, cardW, cardH, null)
      if isSelected && blinkState then
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, Array(10.0f, 10.0f), 0.0f))
        g.setColor(new Color(255, 215, 0))
        g.drawRect(2, yOffset + 2, cardW - 4, cardH - 4)
      else if isHovered && !isSelected then
        g.setStroke(new BasicStroke(4))
        g.setColor(new Color(100, 200, 255))
        g.drawRect(2, yOffset + 2, cardW - 4, cardH - 4)

  /** Updates the player hand.
   */
  def updateHand(cards: List[Card[?]]): Unit =
    cardsContainer.contents.clear()
    selectedPanel = None
    for card <- cards do
      val viewInfo = card.cardToViewInfo
      val template = renderer.assets.frontTemplatePath(viewInfo.cardType)
      renderer.render(viewInfo, template) match
        case Some(cardIcon) =>
          cardsContainer.contents += new HandCardPanel(card, cardIcon.getImage)
          cardsContainer.contents += Swing.HStrut(10)
        case None =>
          println(s"Error in the player hand rendering: ${card.name}")
    revalidate()
    repaint()