package org.scryption.view

import org.scryption.game.model.CardLibrary
import org.scryption.view.CardViewAssets
import org.scryption.view.CardViewInfo
import org.scryption.view.ResourceLoader

import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import scala.swing.*
import scala.swing.event.*

// Displays a row of test cards and flips them between back/front on click.

object FlipCardsApp extends SimpleSwingApplication {

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"

  private val geometry = CardGeometry(cardWidth = 500)
  private val nameFont = ResourceLoader.loadFont(fontPath, geometry.nameFontSize)
  private val statFont = ResourceLoader.loadFont(fontPath, geometry.statFontSize)
  private val renderer = new CardView(geometry, nameFont, statFont)

  // Blank image shown if a template/asset genuinely can't be loaded, so the
  // rest of the UI never has to handle a null Icon.
  private val placeholderIcon: ImageIcon =
    new ImageIcon(new BufferedImage(geometry.cardWidth, geometry.cardHeight, BufferedImage.TYPE_INT_RGB))

  // A few real cards from the library, converted to view info.
  private val testCards: List[CardViewInfo] =
    List(CardLibrary.stoat, CardLibrary.raven, CardLibrary.grizzly).map(_.toViewInfo)

  def top: Frame = new MainFrame {
    title = "Card View Test"

    val backIcon: ImageIcon =
      renderer.render(CardViewInfo("", "", "", ""), CardViewAssets.backTemplatePath).getOrElse(placeholderIcon)

    val frontIcons: List[ImageIcon] =
      testCards.map(card => renderer.render(card, CardViewAssets.frontTemplatePath(card.cardType)).getOrElse(placeholderIcon))

    contents = new BoxPanel(Orientation.Horizontal) {
      border = Swing.EmptyBorder(geometry.borderSize, geometry.borderSize, geometry.borderSize, geometry.borderSize)
      background = Color.WHITE

      frontIcons.foreach { frontIcon =>
        val btn = new Button {
          icon = backIcon
          preferredSize = new Dimension(geometry.cardWidth, geometry.cardHeight)
          maximumSize = preferredSize
          minimumSize = preferredSize
          focusable = false
          border = Swing.EmptyBorder(0, 0, 0, 0)
        }

        btn.reactions += { case ButtonClicked(_) =>
          btn.icon = if (btn.icon == backIcon) frontIcon else backIcon
          btn.peer.repaint()
        }

        contents += btn
        contents += Swing.HStrut(10)
      }
    }
  }
}