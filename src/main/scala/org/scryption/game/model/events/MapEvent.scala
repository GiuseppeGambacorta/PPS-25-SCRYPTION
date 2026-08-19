package org.scryption.game.model.events

import org.scryption.{EventMessages, GameMessagesInterface}
import org.scryption.game.model.{Card, GameState}
import org.scryption.GameEvents.*
import org.scryption.MapMessages

import scala.annotation.tailrec
import scala.language.implicitConversions

implicit def NodeToOption(node: Node): Option[Node] = Some(node)

case class Node(event: GameEvent, nextNode: Option[Node] = None, left: Option[Node]= None, right: Option[Node]= None)


case class GameMap(ActualEvent : GameEvent, Done : List[Node], Left : Node)

object GameMap:
  def apply() : GameMap = {

    val lastPath = Node(getANewItem, Node(fight))
    val left = Node(getANewCard, Node(fireCampAttack, lastPath))
    val right = Node(fireCampHealth, Node(mycologists, lastPath))

    val firstPath = Node(fight, Node(sacrifice, None, left, right))

    GameMap(firstPath.event, Nil, firstPath)
  }



@tailrec
def MapEvent(gameMap: GameMap, ch: GameMessagesInterface): GameMap =
  val current = gameMap.Left

  ch.receiveFromGame match
    case MapMessages.forward if current.nextNode.isDefined =>
      val target = current.nextNode.get
      GameMap(target.event, gameMap.Done :+ current, target)

    case MapMessages.left if current.left.isDefined =>
      val target = current.left.get
      GameMap(target.event, gameMap.Done :+ current, target)

    case MapMessages.right if current.right.isDefined =>
      val target = current.right.get
      GameMap(target.event, gameMap.Done :+ current, target)

    case _ =>
      ch.clear()
      MapEvent(gameMap, ch)

