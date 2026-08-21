package org.scryption.game.model
import org.scryption.GameEvents.{GameEvent, fight, fireCampAttack, fireCampHealth, getANewCard, getANewItem, listOfNotFightEvents, mycologists, sacrifice}
import scala.util.Random
import scala.language.implicitConversions

case class Node(event: GameEvent, nextNode: Option[Node] = None, left: Option[Node]= None, right: Option[Node]= None)

implicit def NodeToOption(node: Node): Option[Node] = Some(node)


case class GameMap(Done: List[Node], Left: Node):
  def currentEvent: GameEvent = Left.event

object GameMap:
  def apply(): GameMap =
    val lastPath = Node(randomEvent, Node(fight))
    val left = Node(randomEvent, Node(randomEvent, lastPath))
    val right = Node(randomEvent, Node(randomEvent, lastPath))



    val firstPath = Node(getANewCard, Node(randomEvent, None, left, right))

    GameMap(Nil, firstPath)

  private def randomEvent : GameEvent = Random.shuffle(listOfNotFightEvents).head