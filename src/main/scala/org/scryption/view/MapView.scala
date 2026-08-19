package org.scryption.view

import org.scryption.view.common.ResourceLoader

import java.awt.{BasicStroke, Color, Dimension, Graphics2D, Point, RenderingHints, Toolkit}
import scala.annotation.unused
import scala.collection.mutable
import scala.swing.*
import scala.swing.event.*

// --- 1. MODELLO DATI ---
sealed trait NodeType {
  def color: Color
  def iconPath: String
}
case object battle extends NodeType { val color = new Color(200, 60, 60); val iconPath = "map/animated_cardbattlenode_1.png" }
case object cardSelection extends NodeType { val color = new Color(220, 180, 50); val iconPath = "map/animated_cardchoicenode_1.png" }
case object fireCampAttack   extends NodeType { val color = new Color(60, 180, 80); val iconPath = "map/animated_campfire_1.png" }
case object fireCampHealth   extends NodeType { val color = new Color(60, 180, 80); val iconPath = "map/animated_campfire_1.png" }
case object mycologist extends NodeType { val color = new Color(140, 80, 200); val iconPath = "map/animated_mushrooms_1.png" }
case object trial   extends NodeType { val color = new Color(7, 180, 186); val iconPath = "map/animated_decktrialnode_1.png" }



case class MapNode(nodeType: NodeType, var visited: Boolean = false, var available: Boolean = false)

// --- 2. CANVAS DI DISEGNO DELLA MAPPA ---
class MapView(@unused viewModelMap: ViewModelMap) extends BorderPanel {
  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  private val squareSize = math.max(400, math.min(screenSize.width, screenSize.height) - 80)
  private val fixedSize = new Dimension(squareSize, squareSize)
  preferredSize = screenSize
  private val backgroundImage = ResourceLoader.loadTemplateImage("map/map.png")
  private val nodeRadius = 25
  private val nodeIconSize = 34

  // Definizione dei Nodi (ordine logico dal basso verso l'alto)
  private val nodes: Vector[MapNode] = Vector(
    MapNode(battle, available = true),
    MapNode(cardSelection),
    MapNode(battle),
    MapNode(fireCampAttack),
    MapNode(battle),
    MapNode(fireCampHealth),
    MapNode(trial),
    MapNode(mycologist),
    MapNode(battle)
  )

  // Grafo delle connessioni (da ID nodo -> lista ID nodi raggiungibili)
  private val connections: Map[Int, List[Int]] = Map(
    0 -> List(1, 2),
    1 -> List(3, 4),
    2 -> List(4, 5),
    3 -> List(6),
    4 -> List(6, 7),
    5 -> List(7),
    6 -> List(8),
    7 -> List(8)
  )

  private val reverseConnections: Map[Int, List[Int]] =
    connections.foldLeft(Map.empty[Int, List[Int]].withDefaultValue(Nil)) { (acc, entry) =>
      val (parent, children) = entry
      children.foldLeft(acc) { (map, child) => map.updated(child, parent :: map(child)) }
    }

  private var currentNode: Option[Int] = None
  private val nodeIcons: Map[NodeType, Option[java.awt.Image]] = List(battle, cardSelection, fireCampAttack, fireCampHealth, mycologist, trial)
    .map(nodeType => nodeType -> ResourceLoader.loadImage(nodeType.iconPath, nodeIconSize))
    .toMap

  private def computeDepths(): Vector[Int] =
    val depths = Array.fill(nodes.size)(Int.MaxValue)
    val queue = mutable.Queue[Int](0)
    depths(0) = 0

    while queue.nonEmpty do
      val current = queue.dequeue()
      val nextDepth = depths(current) + 1
      for child <- connections.getOrElse(current, Nil) do
        if nextDepth < depths(child) then
          depths(child) = nextDepth
          queue.enqueue(child)

    depths.map(d => if d == Int.MaxValue then 0 else d).toVector

  private def layoutPositions(panelSize: Dimension): Vector[Point] =
    val depths = computeDepths()
    val maxDepth = depths.max
    val marginX = math.max(80, (panelSize.width * 0.12).toInt)
    val topMargin = math.max(70, (panelSize.height * 0.10).toInt)
    val bottomMargin = math.max(140, (panelSize.height * 0.18).toInt)

    val rootY = panelSize.height - bottomMargin - nodeRadius
    val topY = topMargin + nodeRadius
    val usableHeight = math.max(1, rootY - topY)
    val stepY = if maxDepth == 0 then 0 else math.max(1, usableHeight / maxDepth)

    val levelNodes: Map[Int, Vector[Int]] = depths.zipWithIndex.groupBy(_._1).map { case (depth, values) =>
      depth -> values.map(_._2).toVector
    }
    val positions = Array.fill(nodes.size)(new Point(panelSize.width / 2, rootY))

    for depth <- 0 to maxDepth do
      val indices = levelNodes.getOrElse(depth, Vector.empty)
      val ordered =
        if depth == 0 then indices
        else indices.sortBy { idx =>
          val parentXs = reverseConnections.getOrElse(idx, Nil).flatMap(parent => positions.lift(parent).map(_.x))
          if parentXs.nonEmpty then parentXs.sum.toDouble / parentXs.size else idx.toDouble
        }

      val count = ordered.size
      val y = rootY - depth * stepY
      val xPositions =
        if count == 0 then Vector.empty
        else if count == 1 then Vector(panelSize.width / 2)
        else
          val availableWidth = math.max(1, panelSize.width - 2 * marginX)
          val spacing = availableWidth.toDouble / (count + 1)
          ordered.indices.map(i => (marginX + ((i + 1) * spacing)).round.toInt).toVector

      ordered.zip(xPositions).foreach { case (idx, x) =>
        positions(idx) = new Point(x, y)
      }

    positions.toVector

  private val mapCanvas: Panel = new Panel {
    preferredSize = fixedSize
    minimumSize = fixedSize
    maximumSize = fixedSize
    opaque = false

    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked =>
        val clickedPoint = e.point
        val positions = layoutPositions(size)
        nodes.zipWithIndex.find { case (node, idx) =>
          node.available && isPointInsideNode(clickedPoint, positions(idx))
        }.foreach { case (_, idx) =>
          selectNode(idx)
        }
    }

    override protected def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      backgroundImage.foreach { img =>
        g.drawImage(img, 0, 0, size.width, size.height, peer)
      }
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      val positions = layoutPositions(size)

      // A. Disegna le connessioni tra i nodi
      val dashedStroke = new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, Array(8f, 6f), 0f)
      g.setStroke(dashedStroke)
      g.setColor(new Color(120, 100, 80, 180))

      for ((fromId, toIds) <- connections) {
        val fromPos = positions(fromId)
        for (toId <- toIds) {
          val toPos = positions(toId)
          g.drawLine(fromPos.x, fromPos.y, toPos.x, toPos.y)
        }
      }

      // B. Disegna i Nodi
      for ((node, idx) <- nodes.zipWithIndex) {
        val nodePos = positions(idx)
        g.setColor(new Color(0, 0, 0, 40))
        g.fillOval(nodePos.x - nodeRadius + 3, nodePos.y - nodeRadius + 3, nodeRadius * 2, nodeRadius * 2)

        g.setColor(node.nodeType.color)
        g.fillOval(nodePos.x - nodeRadius, nodePos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2)

        nodeIcons.getOrElse(node.nodeType, None).foreach { icon =>
          g.drawImage(icon, nodePos.x - nodeIconSize / 2, nodePos.y - nodeIconSize / 2, nodeIconSize, nodeIconSize, null)
        }

        if (node.available) {
          g.setStroke(new BasicStroke(4f))
          g.setColor(new Color(255, 215, 0))
        } else if (node.visited) {
          g.setStroke(new BasicStroke(2f))
          g.setColor(Color.DARK_GRAY)
        } else {
          g.setStroke(new BasicStroke(2f))
          g.setColor(Color.WHITE)
        }
        g.drawOval(nodePos.x - nodeRadius, nodePos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2)
      }

      currentNode.foreach { playerIdx =>
        val player = positions(playerIdx)
        g.setColor(new Color(230, 40, 40))
        g.setStroke(new BasicStroke(3f))
        g.drawOval(player.x - nodeRadius - 6, player.y - nodeRadius - 6, (nodeRadius + 6) * 2, (nodeRadius + 6) * 2)
      }
  }

  private val exitButton: Button = new Button("Esci") {
    opaque = true
    background = new Color(245, 245, 245)
    foreground = Color.BLACK
    borderPainted = true
    focusPainted = false
    preferredSize = new Dimension(140, 48)
  }

  private val saveButton: Button = new Button("Salva partita") {
    opaque = true
    background = new Color(245, 245, 245)
    foreground = Color.BLACK
    borderPainted = true
    focusPainted = false
    preferredSize = new Dimension(160, 48)
  }

  opaque = true
  background = Color.BLACK
  layout(new BorderPanel {
    opaque = true
    background = Color.BLACK
    layout(new BoxPanel(Orientation.Horizontal) {
      opaque = true
      background = Color.BLACK
      contents += Swing.HGlue
      contents += exitButton
      contents += Swing.HGlue
    }) = BorderPanel.Position.North
    layout(new BoxPanel(Orientation.Vertical) {
      opaque = true
      background = Color.BLACK
      contents += Swing.VGlue
    }) = BorderPanel.Position.Center
  }) = BorderPanel.Position.West
  layout(new BorderPanel {
    opaque = true
    background = Color.BLACK
    layout(new BoxPanel(Orientation.Horizontal) {
      opaque = true
      background = Color.BLACK
      contents += Swing.HGlue
      contents += saveButton
      contents += Swing.HGlue
    }) = BorderPanel.Position.North
    layout(new BoxPanel(Orientation.Vertical) {
      opaque = true
      background = Color.BLACK
      contents += Swing.VGlue
    }) = BorderPanel.Position.Center
  }) = BorderPanel.Position.East
  layout(new BoxPanel(Orientation.Vertical) {
    opaque = true
    background = Color.BLACK
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal) {
      opaque = true
      background = Color.BLACK
      contents += Swing.HGlue
      contents += mapCanvas
      contents += Swing.HGlue
    }
    contents += Swing.VGlue
  }) = BorderPanel.Position.Center

  private def isPointInsideNode(p: Point, nodePos: Point): Boolean =
    val dx = p.x - nodePos.x
    val dy = p.y - nodePos.y
    (dx * dx + dy * dy) <= (nodeRadius * nodeRadius)

  private def selectNode(nodeIndex: Int): Unit = {
    currentNode.foreach(nodes(_).visited = true)
    currentNode = Some(nodeIndex)
    nodes(nodeIndex).visited = true

    // Disabilita tutti i nodi
    nodes.foreach(_.available = false)

    // Attiva solo i nodi successivi collegati
    connections.getOrElse(nodeIndex, Nil).foreach(nextIdx => nodes(nextIdx).available = true)

    repaint()
  }
}