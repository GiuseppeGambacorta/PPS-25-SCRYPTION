package org.scryption.view

import org.scryption.GameEvents.GameEvent
import org.scryption.game.model.Maps.Path
import org.scryption.{GameEvents, GameMessagesChannel, MapMessages}

case class ViewNode(
                     node: GameEvent,
                     iconPath: String,
                     row: Int,
                     col: Int
                   )

case class MapConnection(from: ViewNode, to: ViewNode)

class ViewModelMap(val channel: GameMessagesChannel, val gameMap: Path[GameEvent]):

  def toString(event: GameEvent): String = event match
    case GameEvents.getANewCard => "cardchoicenode"
    case GameEvents.fight => "fight"
    case GameEvents.fireCampAttack => "campfire"
    case GameEvents.fireCampHealth => "campfire"
    case GameEvents.mycologists => "mushrooms"
    case GameEvents.sacrifice => "cardmergenode"
    case GameEvents.getANewItem => "backpack"
    case _ => ""

  def getVisibleNodes(maxDepth: Int = 6): List[ViewNode] = {
    def traverse(current: Path[GameEvent], r: Int, c: Int, acc: List[ViewNode]): List[ViewNode] = {
      if (r > maxDepth) acc
      else current match {
        case Path.Node(event, next) =>
          val newNode = ViewNode(event, toString(event), r, c)
          traverse(next, r + 1, c, newNode :: acc)
        case Path.Fork(left, right) =>
          val leftNodes = traverse(left, r + 1, c - 1, acc)
          val rightNodes = traverse(right, r + 1, c + 1, leftNodes)
          rightNodes
        case Path.End() =>
          acc
      }
    }
    traverse(gameMap, 0, 3, Nil).reverse
  }

  def getConnections(nodes: List[ViewNode]): List[MapConnection] = {
    nodes.flatMap { node =>
      nodes.filter(n => n.row == node.row + 1 && (n.col == node.col - 1 || n.col == node.col || n.col == node.col + 1))
        .map(target => MapConnection(node, target))
    }
  }

  val currentNodeData: Option[ViewNode] = gameMap match {
    case Path.Node(event, _) => Some(ViewNode(event, toString(event), 0, 3))
    case _ => None
  }

  def canGoForward: Boolean = gameMap match {
    case Path.Node(_, next) => next match { case Path.Node(_, _) => true; case _ => false }
    case _ => false
  }

  def canGoLeft: Boolean = gameMap match {
    case Path.Node(_, next) => next match { case Path.Fork(_, _) => true; case _ => false }
    case _ => false
  }

  def canGoRight: Boolean = canGoLeft

  def onForward(): Unit = channel.sendToGame(MapMessages.forward)
  def onLeft(): Unit    = channel.sendToGame(MapMessages.left)
  def onRight(): Unit   = channel.sendToGame(MapMessages.right)