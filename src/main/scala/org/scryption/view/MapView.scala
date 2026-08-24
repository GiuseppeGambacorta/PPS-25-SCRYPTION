package org.scryption.view

import org.scryption.view.common.ResourceLoader
import org.scryption.game.model.Maps.Path
import org.scryption.view.common.GUIAssets.MapViewAssets
import org.scryption.GameEvents.GameEvent

import java.awt.{BasicStroke, Color, Cursor, Dimension, Graphics2D, RenderingHints, Toolkit}
import scala.swing.*
import scala.swing.event.*

class MapView(viewModelMap: ViewModelMap, onSave: () => Unit = () => ()) extends BorderPanel:
  preferredSize = Toolkit.getDefaultToolkit.getScreenSize
  val assets = MapViewAssets()

  private val tableImage = ResourceLoader.loadTemplateImage("table.png")
  private val mapBackgroundImage = ResourceLoader.loadTemplateImage("map/map.png")

  private val nodeSize = 100

  private def rowHeight: Double = size.height / 7
  private def colWidth: Double = 170
  private def centerX: Double = size.width / 2.0
  private def startY: Double = size.height * 0.15

  private var interactiveNodes: List[ViewNode] = Nil
  private var hoveredNode: Option[ViewNode] = None

  private val mapCanvas: Panel = new Panel:
    preferredSize = Toolkit.getDefaultToolkit.getScreenSize
    minimumSize = new Dimension(800, 600)
    opaque = false
    cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

    listenTo(mouse.moves, mouse.clicks)

    reactions += {
      case e: MouseMoved =>
        handleHover(e.point)
        repaint()
      case e: MouseClicked =>
        handleClick(e.point)
    }

    private def handleHover(p: java.awt.Point): Unit =
      val futureNodes = interactiveNodes.filter(n => n.row == 1)
      var foundHover = false

      futureNodes.foreach { node =>
        val (x, y) = getCoordinates(node)
        val dx = p.x - x.toInt
        val dy = p.y - y.toInt
        if (dx * dx + dy * dy <= (nodeSize * 0.8) * (nodeSize * 0.8)) then
          hoveredNode = Some(node)
          cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
          foundHover = true
      }

      if (!foundHover) then
        hoveredNode = None
        cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

    private def handleClick(p: java.awt.Point): Unit =
      hoveredNode.foreach { node =>
        if (node.row == 1) then
          val direction = node.col - 3
          direction match
            case 0 =>
              viewModelMap.onForward()
            case n if n < 0 =>
              viewModelMap.onLeft()
            case n if n > 0 =>
              viewModelMap.onRight()
            case _ => ()
      }

    override protected def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      tableImage.foreach { img =>
        g.drawImage(img, 0, 0, size.width, size.height, peer)
      }

      mapBackgroundImage.foreach { img =>
        val imgW = img.getWidth(null)
        val imgH = img.getHeight(null)
        if (imgW > 0 && imgH > 0) then
          val maxWidth = (size.width * 0.9).toInt
          val maxHeight = (size.height * 0.9).toInt
          val scale = math.min(maxWidth.toDouble / imgW, maxHeight.toDouble / imgH)
          val drawW = (imgW * scale).toInt
          val drawH = (imgH * scale).toInt
          val x = (size.width - drawW) / 2
          val y = (size.height - drawH) / 2
          g.drawImage(img, x, y, drawW, drawH, peer)
        end if
      }

      val dashedStroke = new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, Array(12f, 10f), 0f)
      g.setStroke(dashedStroke)
      g.setColor(new Color(50, 40, 30, 200))

      viewModelMap.gameMap match
        case startNode: Path.Node[GameEvent] =>
          interactiveNodes = paintPath(g, startNode, 0, 3)
        case Path.Fork(l, r) =>
          val leftNodes = paintPath(g, l, 1, 1)
          val rightNodes = paintPath(g, r, 1, 5)
          interactiveNodes = leftNodes ++ rightNodes
        case _ =>
          interactiveNodes = Nil

    private def paintPath(g: Graphics2D, current: Path[GameEvent], row: Int, col: Int): List[ViewNode] =
      current match
        case Path.Node(event, next) =>
          val iconKey = viewModelMap.toString(event)
          val node = ViewNode(event, iconKey, row, col)

          drawNode(g, node)

          next match
            case Path.Node(_, _) =>
              val (x1, y1) = getCoordinates(node)
              val (x2, y2) = getCoordinates((row + 1, col))
              g.drawLine(x1.toInt, y1.toInt, x2.toInt, y2.toInt)
              node :: paintPath(g, next, row + 1, col)

            case Path.Fork(leftBranch, rightBranch) =>
              val leftStart = col match
                case 3 => findFirstNodeCoords(leftBranch, row + 1, col - 2)
                case _ => findFirstNodeCoords(leftBranch, row + 1, col - 1)
              val rightStart = col match
                case 3 => findFirstNodeCoords(rightBranch, row + 1, col + 2)
                case _ => findFirstNodeCoords(rightBranch, row + 1, col + 1)

              val (x1, y1) = getCoordinates(node)

              leftStart.foreach { case (x2, y2) =>
                g.drawLine(x1.toInt, y1.toInt, x2.toInt, y2.toInt)
              }

              rightStart.foreach { case (x2, y2) =>
                g.drawLine(x1.toInt, y1.toInt, x2.toInt, y2.toInt)
              }

              val leftNodes = col match
                case 3 => paintPath(g, leftBranch, row + 1, col - 2)
                case _ => paintPath(g, leftBranch, row + 1, col - 1)
              val rightNodes = col match
                case 3 => paintPath(g, rightBranch, row + 1, col + 2)
                case _ => paintPath(g, rightBranch, row + 1, col + 1)

              node :: (leftNodes ++ rightNodes)

            case Path.End() =>
              List(node)

        case Path.Fork(left, right) =>
          col match
            case 3 =>
              val leftNodes = paintPath(g, left, row + 1, col - 2)
              val rightNodes = paintPath(g, right, row + 1, col + 2)
              leftNodes ++ rightNodes
            case _ =>
              val leftNodes = paintPath(g, left, row + 1, col - 1)
              val rightNodes = paintPath(g, right, row + 1, col + 1)
              leftNodes ++ rightNodes

        case Path.End() =>
          Nil
    private def findFirstNodeCoords(p: Path[GameEvent], row: Int, col: Int): Option[(Double, Double)] =
      p match
        case Path.Node(_, _) =>
          Some(getCoordinates((row, col)))
        case Path.Fork(left, right) =>
          col match
            case 3 => findFirstNodeCoords(left, row + 1, col - 2) orElse findFirstNodeCoords(right, row + 1, col + 2)
            case _ => findFirstNodeCoords(left, row + 1, col - 1) orElse findFirstNodeCoords(right, row + 1, col + 1)
        case Path.End() =>
          None

    private def getCoordinates(node: ViewNode): (Double, Double) =
      getCoordinates((node.row, node.col))

    private def getCoordinates(rc: (Int, Int)): (Double, Double) =
      val (r, c) = rc
      val x = centerX + (c - 3) * colWidth
      val y = startY + r * rowHeight
      (x, y)

    private def drawNode(g: Graphics2D, node: ViewNode): Unit =
      val (x, y) = getCoordinates(node)
      val ix = x.toInt
      val iy = y.toInt

      ResourceLoader.loadImage(assets.eventIconPath(node.iconPath), nodeSize).foreach { icon =>
        g.drawImage(icon, ix - nodeSize / 2, iy - nodeSize / 2, nodeSize, nodeSize, null)
      }

  opaque = true
  background = new Color(20, 18, 16)
  layout(mapCanvas) = BorderPanel.Position.Center

  private val saveButton = new Button("Save Game"):
    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    background = new Color(50, 40, 30)
    foreground = Color.RED
    font = ResourceLoader.loadFont("heavyweight-cufonfonts/HEAVYWEI.TTF", 24f)
    tooltip = "Save your progress"

  listenTo(saveButton)
  reactions += { case ButtonClicked(`saveButton`) =>
    onSave()
    saveButton.foreground = Color.RED
    saveButton.text = "Game Saved!"
    saveButton.enabled = false
  }

  private val bottomPanel = new FlowPanel(FlowPanel.Alignment.Left)(saveButton):
    opaque = false
    border = Swing.EmptyBorder(0, 0, 20, 20)

  opaque = true
  background = new Color(20, 18, 16)

  layout(mapCanvas) = BorderPanel.Position.Center
  layout(bottomPanel) = BorderPanel.Position.South
