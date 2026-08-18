package org.scryption.view

import org.scryption.view.common.ResourceLoader

import java.awt.{BasicStroke, Color, Dimension, Graphics2D, Point, RenderingHints, Toolkit}
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



case class MapNode(
                    id: Int,
                    layer: Int,
                    x: Int,
                    y: Int,
                    nodeType: NodeType,
                    var visited: Boolean = false,
                    var available: Boolean = false
                  )

// --- 2. CANVAS DI DISEGNO DELLA MAPPA ---
class InscryptionMapPanel extends Panel {
  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  private val squareSize = math.max(400, math.min(screenSize.width, screenSize.height) - 80)
  private val fixedSize = new Dimension(squareSize, squareSize)
  preferredSize = fixedSize
  minimumSize = fixedSize
  maximumSize = fixedSize
  private val backgroundImage = ResourceLoader.loadTemplateImage("map/map.png")
  private val nodeRadius = 25
  private val nodeIconSize = 34

  // Definizione dei Nodi (Strati da 0 a 4)
  private val nodes: List[MapNode] = List(
    // Layer 0 (Start)
    MapNode(0, 0, 350, 700, battle, available = true),

    // Layer 1
    MapNode(1, 1, 200, 550, cardSelection),
    MapNode(2, 1, 500, 550, battle),

    // Layer 2
    MapNode(3, 2, 150, 400, fireCampAttack),
    MapNode(4, 2, 350, 400, battle),
    MapNode(5, 2, 550, 400, fireCampHealth),

    // Layer 3
    MapNode(6, 3, 250, 250, trial),
    MapNode(7, 3, 450, 250, mycologist),

    // Layer 4 (Boss)
    MapNode(8, 4, 350, 100, battle)
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

  private var currentNode: Option[MapNode] = None
  private val nodeIcons: Map[NodeType, Option[java.awt.Image]] = List(battle, cardSelection, fireCampAttack, fireCampHealth, mycologist, trial)
    .map(nodeType => nodeType -> ResourceLoader.loadImage(nodeType.iconPath, nodeIconSize))
    .toMap

  private val nodeBoundsLeft = nodes.map(_.x).min - nodeRadius
  private val nodeBoundsTop = nodes.map(_.y).min - nodeRadius
  private val nodeBoundsWidth = nodes.map(_.x).max - nodes.map(_.x).min + nodeRadius * 2
  private val nodeBoundsHeight = nodes.map(_.y).max - nodes.map(_.y).min + nodeRadius * 2

  private def nodeOffset: (Int, Int) =
    val offsetX = math.max(0, (size.width - nodeBoundsWidth) / 2 - nodeBoundsLeft)
    val offsetY = math.max(0, (size.height - nodeBoundsHeight) / 2 - nodeBoundsTop)
    (offsetX, offsetY)

  // Riconoscimento click sul nodo
  listenTo(mouse.clicks)
  reactions += {
    case e: MouseClicked =>
      val clickedPoint = e.point
      val (offsetX, offsetY) = nodeOffset
      nodes.find(n => n.available && isPointInsideNode(clickedPoint, n, offsetX, offsetY)).foreach { chosenNode =>
        selectNode(chosenNode)
      }
  }

  private def isPointInsideNode(p: Point, node: MapNode, offsetX: Int, offsetY: Int): Boolean = {
    val dx = p.x - (node.x + offsetX)
    val dy = p.y - (node.y + offsetY)
    (dx * dx + dy * dy) <= (nodeRadius * nodeRadius)
  }

  private def selectNode(node: MapNode): Unit = {
    currentNode.foreach(_.visited = true)
    currentNode = Some(node)
    node.visited = true

    // Disabilita tutti i nodi
    nodes.foreach(_.available = false)

    // Attiva solo i nodi successivi collegati
    val nextIds = connections.getOrElse(node.id, Nil)
    nodes.filter(n => nextIds.contains(n.id)).foreach(_.available = true)

    repaint()
  }




  // --- RENDERING GRAFICO (Graphics2D) ---
  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    backgroundImage.foreach { img =>
      g.drawImage(img, 0, 0, size.width, size.height, peer)
    }
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val (offsetX, offsetY) = nodeOffset

    // A. Disegna le connessioni tra i nodi
    val dashedStroke = new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, Array(8f, 6f), 0f)
    g.setStroke(dashedStroke)
    g.setColor(new Color(120, 100, 80, 180))

    for ((fromId, toIds) <- connections) {
      val fromNode = nodes.find(_.id == fromId).get
      for (toId <- toIds) {
        val toNode = nodes.find(_.id == toId).get
        // Disegna linea
        g.drawLine(fromNode.x + offsetX, fromNode.y + offsetY, toNode.x + offsetX, toNode.y + offsetY)
      }
    }

    // B. Disegna i Nodi
    for (node <- nodes) {
      // Ombra
      g.setColor(new Color(0, 0, 0, 40))
       g.fillOval(node.x + offsetX - nodeRadius + 3, node.y + offsetY - nodeRadius + 3, nodeRadius * 2, nodeRadius * 2)

      // Corpo del nodo
      g.setColor(node.nodeType.color)
      g.fillOval(node.x + offsetX - nodeRadius, node.y + offsetY - nodeRadius, nodeRadius * 2, nodeRadius * 2)

      nodeIcons.getOrElse(node.nodeType, None).foreach { icon =>
        g.drawImage(icon, node.x + offsetX - nodeIconSize / 2, node.y + offsetY - nodeIconSize / 2, nodeIconSize, nodeIconSize, null)
      }

      // Bordo in base allo stato
      if (node.available) {
        g.setStroke(new BasicStroke(4f))
        g.setColor(new Color(255, 215, 0)) // Oro se selezionalbile
      } else if (node.visited) {
        g.setStroke(new BasicStroke(2f))
        g.setColor(Color.DARK_GRAY)
      } else {
        g.setStroke(new BasicStroke(2f))
        g.setColor(Color.WHITE)
      }
      g.drawOval(node.x + offsetX - nodeRadius, node.y + offsetY - nodeRadius, nodeRadius * 2, nodeRadius * 2)

    }

    // C. Evidenzia la posizione attuale del giocatore
    currentNode.foreach { player =>
      g.setColor(new Color(230, 40, 40))
      g.setStroke(new BasicStroke(3f))
      g.drawOval(player.x + offsetX - nodeRadius - 6, player.y + offsetY - nodeRadius - 6, (nodeRadius + 6) * 2, (nodeRadius + 6) * 2)
    }
  }
}

// --- 3. APPLICAZIONE PRINCIPALE ---
object InscryptionMapApp extends SimpleSwingApplication {
  val top: MainFrame = new MainFrame {
    title = "Inscryption Map Flow - Scala Swing"
    val mapPanel = new InscryptionMapPanel
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


    contents = new BorderPanel {
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
          contents += mapPanel
          contents += Swing.HGlue
        }
        contents += Swing.VGlue
      }) = BorderPanel.Position.Center
    }
    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)
    centerOnScreen()
  }
}