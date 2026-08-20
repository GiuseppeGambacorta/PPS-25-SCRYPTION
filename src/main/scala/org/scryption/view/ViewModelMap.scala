package org.scryption.view

import org.scryption.game.model.{GameMap, Node}
import org.scryption.{GameMessagesChannel, MapMessages}
import java.awt.Color

case class ViewNode(
                     node: Node,
                     nodeType: NodeType
                   )

sealed trait NodeType:
  def color: Color
  def iconPath: String

case object fightNode extends NodeType:
  val color: Color = new Color(200, 60, 60)
  val iconPath: String = "map/animated_cardbattlenode_1.png"

case object getANewCardNode extends NodeType:
  val color: Color = new Color(220, 180, 50)
  val iconPath: String = "map/animated_cardchoicenode_1.png"

case object fireCampAttackNode extends NodeType:
  val color: Color = new Color(60, 180, 80)
  val iconPath: String = "map/animated_campfire_1.png"

case object fireCampHealthNode extends NodeType:
  val color: Color = new Color(60, 180, 80)
  val iconPath: String = "map/animated_campfire_1.png"

case object mycologistsNode extends NodeType:
  val color: Color = new Color(140, 80, 200)
  val iconPath: String = "map/animated_mushrooms_1.png"

case object TrialNode extends NodeType:
  val color: Color = new Color(7, 180, 186)
  val iconPath: String = "map/animated_decktrialnode_1.png"

case object NewItemNode extends NodeType:
  val color: Color = new Color(220, 180, 50)
  val iconPath: String = "map/animated_backpack_1.png"

case object SacrificeNode extends NodeType:
  val color: Color = new Color(220, 180, 50)
  val iconPath: String = "map/animated_cardmergenode_1.png"

class ViewModelMap(val channel: GameMessagesChannel, val gameMap: GameMap):

  import org.scryption.GameEvents.*

  private def toNodeType(node: Node): NodeType =
    node.event match
      case `fight`          => fightNode
      case `getANewCard`    => getANewCardNode
      case `fireCampAttack` => fireCampAttackNode
      case `fireCampHealth` => fireCampHealthNode
      case `mycologists`    => mycologistsNode
      case `sacrifice`      => SacrificeNode
      case `getANewItem`    => NewItemNode
      case _                => fightNode

  val currentNode: ViewNode = ViewNode(gameMap.Left, toNodeType(gameMap.Left))

  val forwardOption: Option[ViewNode] = gameMap.Left.nextNode.map(n => ViewNode(n, toNodeType(n)))
  val leftOption: Option[ViewNode]    = gameMap.Left.left.map(n => ViewNode(n, toNodeType(n)))
  val rightOption: Option[ViewNode]   = gameMap.Left.right.map(n => ViewNode(n, toNodeType(n)))

  def canGoForward: Boolean = forwardOption.isDefined
  def canGoLeft: Boolean    = leftOption.isDefined
  def canGoRight: Boolean   = rightOption.isDefined

  def onForward(): Unit = channel.sendToGame(MapMessages.forward)
  def onLeft(): Unit    = channel.sendToGame(MapMessages.left)
  def onRight(): Unit   = channel.sendToGame(MapMessages.right)