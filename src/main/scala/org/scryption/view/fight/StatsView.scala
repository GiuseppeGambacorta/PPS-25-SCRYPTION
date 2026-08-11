package org.scryption.view.fight

import org.scryption.GUIChannelInterface
import org.scryption.view.ResourceLoader

import scala.swing.*
import scala.swing.event.ButtonClicked
import java.awt.{Color, Cursor, Dimension, Font}

class StatsView(channel: GUIChannelInterface) extends BoxPanel(Orientation.Vertical):

  opaque = true
  background = new Color(25, 25, 30)
  preferredSize = new Dimension(220, 0)
  border = Swing.EmptyBorder(60, 20, 20, 20)

  private def loadBellIcon(path: String): javax.swing.ImageIcon =
    ResourceLoader.loadTemplateImage(path) match
      case Some(img) =>
        val size = 65
        val scaled = img.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)
        new javax.swing.ImageIcon(scaled)
      case None =>
        new javax.swing.ImageIcon()

  val scaleLabel = new Label("Scale"):
    foreground = Color.WHITE
    font = new Font("SansSerif", Font.BOLD, 16)
    xLayoutAlignment = 0.5

  val scaleValueLabel = new Label("0 / 5"):
    foreground = Color.WHITE
    font = new Font("SansSerif", Font.BOLD, 24)
    xLayoutAlignment = 0.5

  val bonesLabel = new Label("Bones: 0"):
    foreground = new Color(220, 220, 200)
    font = new Font("SansSerif", Font.BOLD, 18)
    xLayoutAlignment = 0.5

  val endTurnButton = new Button(""):
    icon = loadBellIcon("board/bell.png")
    cursor = new Cursor(Cursor.HAND_CURSOR)
    tooltip = "End Turn"
    xLayoutAlignment = 0.5
    val buttonSize = new Dimension(100, 100)
    preferredSize = buttonSize
    maximumSize = buttonSize
    minimumSize = buttonSize
    peer.setContentAreaFilled(false)
    peer.setBorderPainted(false)
    peer.setFocusPainted(false)

    override protected def paintComponent(g: Graphics2D): Unit =
      // anti-aliasing to have a smooth button
      g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
      g.setColor(new Color(150, 50, 50))
      g.fillOval(0, 0, size.width, size.height)
      super.paintComponent(g)

  contents += scaleLabel
  contents += Swing.VStrut(10)
  contents += scaleValueLabel
  contents += Swing.VStrut(60)
  contents += bonesLabel
  contents += Swing.VGlue
  contents += endTurnButton

  listenTo(endTurnButton)
  reactions += {
    case ButtonClicked(`endTurnButton`) =>
      println("UI input: end turn")
    //channel.sendToGame(GUIMessages.EndTurn)
  }

  /** Updates the bones counter.
   */
  def updateBones(bones: Int): Unit =
    bonesLabel.text = s"Bones: $bones"

  /** Updates the scale text.
   *
   * @param balance Positive value is damage in your favor, negative is damage sustained by the player.
   */
  def updateScale(balance: Int): Unit =
    val displayValue = Math.abs(balance)
    scaleValueLabel.text = s"$displayValue / 5"

    if balance == 0 then
      scaleValueLabel.foreground = Color.WHITE
    else if balance > 0 then
      scaleValueLabel.foreground = new Color(100, 255, 100)
    else
      scaleValueLabel.foreground = new Color(255, 100, 100)
