package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.game.model.Card
import org.scryption.view.{CardGeometry, CardView, CardViewAssets, ResourceLoader, toViewInfo}
import scala.swing.*
import scala.swing.event.{ButtonClicked, MouseClicked, MouseEntered, MouseExited}
import java.awt.{Color, Dimension, Cursor}
import javax.swing.border.LineBorder

class HandView(channel: GUIChannelInterface) extends BorderPanel:

  opaque = false

  val toggleButton = new Button("Hide Hand"):
    cursor = new Cursor(Cursor.HAND_CURSOR)
    background = new Color(50, 50, 55)
    foreground = Color.WHITE
    font = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14)

  val cardsContainer = new BoxPanel(Orientation.Horizontal):
    opaque = true
    background = new Color(30, 30, 35)
    border = Swing.EmptyBorder(15, 15, 15, 15)

  val scrollPane = new ScrollPane(cardsContainer):
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Never
    preferredSize = new Dimension(0, 320)

  layout(toggleButton) = BorderPanel.Position.North
  layout(scrollPane) = BorderPanel.Position.Center

  listenTo(toggleButton)
  reactions += {
    case ButtonClicked(`toggleButton`) =>
      scrollPane.visible = !scrollPane.visible
      toggleButton.text = if scrollPane.visible then "Hide Hand" else "Show Hand"
      revalidate()
      repaint()
  }

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val geometry = CardGeometry(cardWidth = 180)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  /** Updates the player hand.
   */
  def updateHand(cards: List[Card[?]]): Unit =
    cardsContainer.contents.clear()

    for card <- cards do
      val viewInfo = card.toViewInfo
      val template = CardViewAssets.frontTemplatePath(viewInfo.cardType)

      renderer.render(viewInfo, template) match
        case Some(cardIcon) =>
          val cardLabel = new Label:
            icon = cardIcon
            cursor = new Cursor(Cursor.HAND_CURSOR)
            border = Swing.EmptyBorder(4, 4, 4, 4)

          listenTo(cardLabel.mouse.clicks, cardLabel.mouse.moves)

          reactions += {
            case MouseEntered(`cardLabel`, _, _) =>
              cardLabel.border = LineBorder(new Color(100, 200, 255), 4, true)

            case MouseExited(`cardLabel`, _, _) =>
              cardLabel.border = Swing.EmptyBorder(4, 4, 4, 4)

            case MouseClicked(`cardLabel`, _, _, _, _) =>
              println(s"UI input: the player has selected [${card.name}] from the hand!")
            //channel.sendToGame(GUIMessages.PlayCard(card))
          }

          cardsContainer.contents += cardLabel
          cardsContainer.contents += Swing.HStrut(10)

        case None =>
          println(s"Error in the player hand rendering: ${card.name}")

    revalidate()
    repaint()