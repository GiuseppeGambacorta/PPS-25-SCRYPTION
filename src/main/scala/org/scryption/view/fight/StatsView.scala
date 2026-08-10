package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import scala.swing.*
import java.awt.{Color, Dimension, Font}

class StatsView(channel: GUIChannelInterface) extends BoxPanel(Orientation.Vertical):

  opaque = true
  background = new Color(25, 25, 30)
  preferredSize = new Dimension(220, 0)
  border = Swing.EmptyBorder(60, 20, 20, 20)

  val scaleLabel = new Label("Scale: Draw"):
    foreground = Color.WHITE
    font = new Font("SansSerif", Font.BOLD, 16)
    xLayoutAlignment = 0.5

  val bonesLabel = new Label("Bones: 0"):
    foreground = new Color(220, 220, 200)
    font = new Font("SansSerif", Font.BOLD, 18)
    xLayoutAlignment = 0.5

  contents += scaleLabel
  contents += Swing.VStrut(80)
  contents += bonesLabel
  contents += Swing.VGlue

  /** Updates the bones counter.
   */
  def updateBones(bones: Int): Unit =
    bonesLabel.text = s"Bones: $bones"

  /** Updates the scale text.
   *
   * @param balance Positive value is damage in your favor, negative is damage sustained by the player.
   */
  def updateScale(balance: Int): Unit = balance match
    case balance if balance == 0 =>
      scaleLabel.text = "Scale: Draw"
      scaleLabel.foreground = Color.WHITE
    case balance if balance > 0 =>
      scaleLabel.text = s"Winning: +$balance"
      scaleLabel.foreground = new Color(100, 255, 100)
    case _ =>
      scaleLabel.text = s"Losing: $balance"
      scaleLabel.foreground = new Color(255, 100, 100)
