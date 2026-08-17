package org.scryption.view.fight

import org.scryption.{FightMessages, GUIChannelInterface, CardMessage}
import org.scryption.game.model.events.TurnState
import org.scryption.view.common.ResourceLoader

import scala.swing.*
import scala.swing.event.ButtonClicked
import java.awt.{Color, Cursor, Dimension}

class StatsView(channel: GUIChannelInterface) extends BoxPanel(Orientation.Vertical):

  var interactable = true
  opaque = true
  background = new Color(25, 25, 30)
  preferredSize = new Dimension(220, 0)
  border = Swing.EmptyBorder(60, 20, 20, 20)

  private val fontPath = "heavyweight-cufonfonts/HEAVYWEI.TTF"
  private val scaleValFont = ResourceLoader.loadFont(fontPath, 24f)
  private val scaleStatFont = ResourceLoader.loadFont(fontPath, 20f)
  private val turnFont = ResourceLoader.loadFont(fontPath, 20f)
  private val bonesFont = ResourceLoader.loadFont(fontPath, 20f)

  private def loadScaledIcon(path: String, width: Int, height: Int): javax.swing.ImageIcon =
    ResourceLoader.loadTemplateImage(path) match
      case Some(img) =>
        val scaled = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
        new javax.swing.ImageIcon(scaled)
      case None =>
        new javax.swing.ImageIcon()

  private val scaleIconLabel = new Label:
    icon = loadScaledIcon("board/scale.png", 100, 100)
    xLayoutAlignment = 0.5
    border = Swing.EmptyBorder(0, 0, 10, 0)

  private val scaleValueLabel = new Label("0 / 5"):
    foreground = Color.WHITE
    font = scaleValFont
    xLayoutAlignment = 0.5

  private val scaleStatusLabel = new Label("Draw"):
    foreground = Color.WHITE
    font = scaleStatFont
    xLayoutAlignment = 0.5

  private val bonesLabel = new Label("Bones 0"):
    foreground = new Color(220, 220, 200)
    font = bonesFont
    xLayoutAlignment = 0.5

  private val turnPhaseLabel = new Label("Phase Draw"):
    foreground = new Color(100, 200, 255)
    font = turnFont
    xLayoutAlignment = 0.5

  private val endTurnButton = new Button(""):
    icon = loadScaledIcon("board/bell.png", 65, 65)
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
      val isPressed = peer.getModel.isPressed
      if isPressed then
        g.setColor(new Color(110, 30, 30))
      else
        g.setColor(new Color(150, 50, 50))
      g.fillOval(0, 0, size.width, size.height)
      if isPressed then
        g.translate(0, 3)
        super.paintComponent(g)
        g.translate(0, -3)
      else
        super.paintComponent(g)

  contents += scaleIconLabel
  contents += scaleValueLabel
  contents += scaleStatusLabel
  contents += Swing.VStrut(40)
  contents += turnPhaseLabel
  contents += Swing.VStrut(20)
  contents += bonesLabel
  contents += Swing.VGlue
  contents += endTurnButton

  listenTo(endTurnButton)
  reactions += {
    case ButtonClicked(`endTurnButton`) =>
      if interactable then
        println("UI input: end turn")
        channel.sendToGame(FightMessages.EndPlayerTurn)
  }

  /** Updates the bones counter.
   */
  def updateBones(bones: Int): Unit =
    bonesLabel.text = s"Bones $bones"

  /** Updates the scale text.
   *
   * @param balance Positive value is damage in your favor, negative is damage sustained by the player.
   */
  def updateScale(balance: Int): Unit =
    val displayValue = Math.abs(balance)
    scaleValueLabel.text = s"$displayValue / 5"
    if balance == 0 then
      scaleValueLabel.foreground = Color.WHITE
      scaleStatusLabel.text = "Draw"
      scaleStatusLabel.foreground = Color.WHITE
    else if balance > 0 then
      scaleValueLabel.foreground = new Color(100, 255, 100)
      scaleStatusLabel.text = "Winning!"
      scaleStatusLabel.foreground = new Color(100, 255, 100)
    else
      scaleValueLabel.foreground = new Color(255, 100, 100)
      scaleStatusLabel.text = "Losing!"
      scaleStatusLabel.foreground = new Color(255, 100, 100)

  def updateTurn(turn: TurnState): Unit =
    turn match
      case TurnState.draw =>
        turnPhaseLabel.text = "Phase Draw"
        turnPhaseLabel.foreground = new Color(100, 200, 255)
      case TurnState.playerTurn =>
        turnPhaseLabel.text = "Phase Play Cards"
        turnPhaseLabel.foreground = new Color(100, 255, 100)
      case TurnState.playerFight =>
        turnPhaseLabel.text = "Phase Player Attack"
        turnPhaseLabel.foreground = new Color(255, 200, 100)
      case TurnState.botTurn =>
        turnPhaseLabel.text = "Phase Bot Plays"
        turnPhaseLabel.foreground = new Color(255, 100, 100)
      case TurnState.botFight =>
        turnPhaseLabel.text = "Phase Bot Attack"
        turnPhaseLabel.foreground = new Color(200, 50, 50)
