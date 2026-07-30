package org.scryption.view

import org.scryption.view.CardViewAssets
import org.scryption.view.CardViewInfo
import org.scryption.view.ResourceLoader

import java.awt.Color
import java.awt.Dimension
import scala.swing.*
import scala.swing.event.*

// Displays a row of test cards and flips them between back/front on click.

object FlipCardsApp extends SimpleSwingApplication {

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"

  private val geometry = CardGeometry(cardWidth = 500)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  // Test cards

  private val testCards = List(
    CardViewInfo(name = "Stoat", cost = "1blood", attack = "1", health = "2"),
    CardViewInfo(
      name = "Mole Man",
      cost = "1blood",
      attack = "0",
      health = "6",
      sigils = List("whackamole", "reach"),
      cardType = "rare"
    ),
    CardViewInfo(name = "Raven", cost = "2blood", attack = "2", health = "3", sigils = List("flying"))
  )

  def top: Frame = new MainFrame {
    title = "Card View Test"

    val backIcon = renderer.render(CardViewInfo("", "", "", ""), CardViewAssets.backTemplatePath).orNull

    val frontIcons = testCards.map(card => renderer.render(card, CardViewAssets.frontTemplatePath(card.cardType)))

    contents = new BoxPanel(Orientation.Horizontal) {
      border = Swing.EmptyBorder(geometry.borderSize, geometry.borderSize, geometry.borderSize, geometry.borderSize)
      background = Color.WHITE

      frontIcons.foreach { frontIconOpt =>
        val btn = new Button {
          icon = backIcon
          preferredSize = new Dimension(geometry.cardWidth, geometry.cardHeight)
          maximumSize = preferredSize
          minimumSize = preferredSize
          focusable = false
          border = Swing.EmptyBorder(0, 0, 0, 0)
        }

        btn.reactions += { case ButtonClicked(_) =>
          btn.icon = if (btn.icon == backIcon) frontIconOpt.orNull else backIcon
          btn.peer.repaint()
        }

        contents += btn
        contents += Swing.HStrut(10)
      }
    }
  }
}
