package org.scryption.view.events

import org.scryption.view.*

import java.awt.{BasicStroke, Color, Dimension, Graphics2D, Point, RenderingHints}
import scala.collection.mutable
import scala.swing.*
import scala.swing.event.*

// --- 1. MODELLO DATI ---
sealed trait NodeType {
  def name: String
  def color: Color
}
case object Battle extends NodeType { val name = "Combattimento"; val color = new Color(200, 60, 60) }
case object Shop   extends NodeType { val name = "Mercante";     val color = new Color(220, 180, 50) }
case object Rest   extends NodeType { val name = "Bivacco";      val color = new Color(60, 180, 80) }
case object Mystery extends NodeType { val name = "Evento ?";    val color = new Color(140, 80, 200) }
case object Boss   extends NodeType { val name = "BOSS";        val color = new Color(30, 30, 30) }

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
  preferredSize = new Dimension(700, 800)
  background = new Color(235, 225, 205) // Colore pergamena stilizzata

  // Definizione dei Nodi (Strati da 0 a 4)
  val nodes: List[MapNode] = List(
    // Layer 0 (Start)
    MapNode(0, 0, 350, 700, Battle, available = true),

    // Layer 1
    MapNode(1, 1, 200, 550, Mystery),
    MapNode(2, 1, 500, 550, Battle),

    // Layer 2
    MapNode(3, 2, 150, 400, Shop),
    MapNode(4, 2, 350, 400, Battle),
    MapNode(5, 2, 550, 400, Rest),

    // Layer 3
    MapNode(6, 3, 250, 250, Mystery),
    MapNode(7, 3, 450, 250, Shop),

    // Layer 4 (Boss)
    MapNode(8, 4, 350, 100, Boss)
  )

  // Grafo delle connessioni (da ID nodo -> lista ID nodi raggiungibili)
  val connections: Map[Int, List[Int]] = Map(
    0 -> List(1, 2),
    1 -> List(3, 4),
    2 -> List(4, 5),
    3 -> List(6),
    4 -> List(6, 7),
    5 -> List(7),
    6 -> List(8),
    7 -> List(8)
  )

  var currentNode: Option[MapNode] = None

  // Riconoscimento click sul nodo
  listenTo(mouse.clicks)
  reactions += {
    case e: MouseClicked =>
      val clickedPoint = e.point
      nodes.find(n => n.available && isPointInsideNode(clickedPoint, n)).foreach { chosenNode =>
        selectNode(chosenNode)
      }
  }

  private def isPointInsideNode(p: Point, node: MapNode): Boolean = {
    val radius = 25
    val dx = p.x - node.x
    val dy = p.y - node.y
    (dx * dx + dy * dy) <= (radius * radius)
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
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    // A. Disegna le connessioni tra i nodi
    val dashedStroke = new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, Array(8f, 6f), 0f)
    g.setStroke(dashedStroke)
    g.setColor(new Color(120, 100, 80, 180))

    for ((fromId, toIds) <- connections) {
      val fromNode = nodes.find(_.id == fromId).get
      for (toId <- toIds) {
        val toNode = nodes.find(_.id == toId).get
        // Disegna linea
        g.drawLine(fromNode.x, fromNode.y, toNode.x, toNode.y)
      }
    }

    // B. Disegna i Nodi
    val radius = 25
    for (node <- nodes) {
      // Ombra
      g.setColor(new Color(0, 0, 0, 40))
      g.fillOval(node.x - radius + 3, node.y - radius + 3, radius * 2, radius * 2)

      // Corpo del nodo
      g.setColor(node.nodeType.color)
      g.fillOval(node.x - radius, node.y - radius, radius * 2, radius * 2)

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
      g.drawOval(node.x - radius, node.y - radius, radius * 2, radius * 2)

      // Testo/Tipo nodo
      g.setColor(Color.WHITE)
      val label = node.nodeType.name.take(1) // Iniziale
      g.drawString(label, node.x - 4, node.y + 5)
    }

    // C. Evidenzia la posizione attuale del giocatore
    currentNode.foreach { player =>
      g.setColor(new Color(230, 40, 40))
      g.setStroke(new BasicStroke(3f))
      g.drawOval(player.x - radius - 6, player.y - radius - 6, (radius + 6) * 2, (radius + 6) * 2)
    }
  }
}

// --- 3. APPLICAZIONE PRINCIPALE ---
object InscryptionMapApp extends SimpleSwingApplication {
  val top: MainFrame = new MainFrame {
    title = "Inscryption Map Flow - Scala Swing"
    contents = new InscryptionMapPanel
    centerOnScreen()
  }
}