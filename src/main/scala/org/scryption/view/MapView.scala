package org.scryption.view

import org.scryption.game.model.Node
import org.scryption.view.common.ResourceLoader

import java.awt.{BasicStroke, Color, Dimension, Font, Graphics2D, Point, RenderingHints, Toolkit}
import scala.swing.*
import scala.swing.event.*

class MapView(viewModelMap: ViewModelMap) extends BorderPanel:
  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  private val squareSize = math.max(400, math.min(screenSize.width, screenSize.height) - 140)
  private val fixedSize = new Dimension(squareSize, squareSize)
  preferredSize = screenSize

  private val backgroundImage = ResourceLoader.loadTemplateImage("map/map.png")
  private val nodeRadius = 28
  private val nodeIconSize = 38


  private val nodeIcons: Map[NodeType, Option[java.awt.Image]] = List(
    fightNode,
    getANewCardNode,
    fireCampAttackNode,
    fireCampHealthNode,
    mycologistsNode,
    TrialNode,
    SacrificeNode,
    NewItemNode
  ).map(nt => nt -> ResourceLoader.loadImage(nt.iconPath, nodeIconSize)).toMap

  private val mapCanvas: Panel = new Panel:
    preferredSize = fixedSize
    minimumSize = fixedSize
    maximumSize = fixedSize
    opaque = false

    override protected def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      backgroundImage.foreach { img =>
        g.drawImage(img, 0, 0, size.width, size.height, peer)
      }
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      val cx = size.width / 2
      val cy = size.height / 2


      val currentPos = Point(cx, (size.height * 0.72).toInt)
      val forwardPos = Point(cx, (size.height * 0.28).toInt)
      val leftPos    = Point((size.width * 0.24).toInt, (size.height * 0.38).toInt)
      val rightPos   = Point((size.width * 0.76).toInt, (size.height * 0.38).toInt)

      val dashedStroke = new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, Array(8f, 6f), 0f)
      g.setStroke(dashedStroke)
      g.setColor(new Color(120, 100, 80, 200))


      if viewModelMap.leftOption.isDefined then g.drawLine(currentPos.x, currentPos.y, leftPos.x, leftPos.y)
      if viewModelMap.forwardOption.isDefined then g.drawLine(currentPos.x, currentPos.y, forwardPos.x, forwardPos.y)
      if viewModelMap.rightOption.isDefined then g.drawLine(currentPos.x, currentPos.y, rightPos.x, rightPos.y)

      def drawNode(pos: Point, nType: NodeType, isCurrent: Boolean, isAvailable: Boolean): Unit =

        g.setColor(new Color(0, 0, 0, 60))
        g.fillOval(pos.x - nodeRadius + 3, pos.y - nodeRadius + 3, nodeRadius * 2, nodeRadius * 2)


        g.setColor(nType.color)
        g.fillOval(pos.x - nodeRadius, pos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2)


        nodeIcons.getOrElse(nType, None).foreach { icon =>
          g.drawImage(icon, pos.x - nodeIconSize / 2, pos.y - nodeIconSize / 2, nodeIconSize, nodeIconSize, null)
        }


        if isCurrent then
          g.setColor(new Color(230, 40, 40))
          g.setStroke(new BasicStroke(4f))
          g.drawOval(pos.x - nodeRadius - 5, pos.y - nodeRadius - 5, (nodeRadius + 5) * 2, (nodeRadius + 5) * 2)
        else if isAvailable then
          g.setColor(new Color(255, 215, 0))
          g.setStroke(new BasicStroke(3.5f))
          g.drawOval(pos.x - nodeRadius, pos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2)


      viewModelMap.leftOption.foreach(vn => drawNode(leftPos, vn.nodeType, false, true))
      viewModelMap.forwardOption.foreach(vn => drawNode(forwardPos, vn.nodeType, false, true))
      viewModelMap.rightOption.foreach(vn => drawNode(rightPos, vn.nodeType, false, true))


      drawNode(currentPos, viewModelMap.currentNode.nodeType, true, false)

  private def createNavButton(label: String, isAvailable: Boolean, onClick: () => Unit): Button =
    new Button(label):
      enabled = isAvailable
      preferredSize = new Dimension(160, 48)
      font = new Font("SansSerif", Font.BOLD, 15)
      opaque = true
      focusPainted = false
      if isAvailable then
        background = new Color(235, 235, 235)
        foreground = Color.BLACK
      else
        background = new Color(60, 60, 60)
        foreground = new Color(140, 140, 140)

      listenTo(this)
      reactions += {
        case ButtonClicked(_) => onClick()
      }

  private val leftButton    = createNavButton("⬅ Sinistra", viewModelMap.canGoLeft, () => viewModelMap.onLeft())
  private val forwardButton = createNavButton("⬆ Avanti", viewModelMap.canGoForward, () => viewModelMap.onForward())
  private val rightButton   = createNavButton("Destra ➡", viewModelMap.canGoRight, () => viewModelMap.onRight())

  private val controlPanel = new BoxPanel(Orientation.Horizontal):
    opaque = true
    background = Color.BLACK
    contents += Swing.HGlue
    contents += leftButton
    contents += Swing.HStrut(20)
    contents += forwardButton
    contents += Swing.HStrut(20)
    contents += rightButton
    contents += Swing.HGlue

  opaque = true
  background = Color.BLACK
  layout(new BoxPanel(Orientation.Vertical) {
    opaque = true
    background = Color.BLACK
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal) {
      opaque = true
      background = Color.BLACK
      contents += Swing.HGlue += mapCanvas += Swing.HGlue
    }
    contents += Swing.VStrut(24)
    contents += controlPanel
    contents += Swing.VGlue
  }) = BorderPanel.Position.Center