package org.scryption.view

import org.scryption.GameEvents.GameEvent
import org.scryption.game.model.Maps.Path
import org.scryption.{GameMessagesChannel, MapMessages}

import java.awt.Color

case class ViewNode(
                     node: GameEvent,
                     iconPath: String
                   )

class ViewModelMap(val channel: GameMessagesChannel, val gameMap: Path[GameEvent]):

  def toString(event: GameEvent): String = event match
    case getANewCard => "cardchoicenode"
    case fight => "fight"
    case fireCampAttack => "campfire"
    case fireCampHealth => "campfire"
    case mycologists => "mushrooms"
    case sacrifice => "cardmergenode"
    case getANewItem => "backpack"
    case _ => ""

  val currentNode: ViewNode           = gameMap match
    case Path.Node(event, _) => ViewNode(event, toString(event))

  val forwardOption: Option[ViewNode] = gameMap match
    case Path.Node(_, next) => next match
      case Path.Node(event, _) => Some(ViewNode(event, toString(event)))
  
  val leftOption: Option[ViewNode]    = gameMap match
    case Path.Node(_, next) => next match
      case Path.Fork(left, _) => left match
        case Path.Node(event, _) => Some(ViewNode(event, toString(event)))
        
  val rightOption: Option[ViewNode]   = gameMap match
    case Path.Node(_, next) => next match
      case Path.Fork(_, right) => right match
        case Path.Node(event, _) => Some(ViewNode(event, toString(event)))

  def canGoForward: Boolean = forwardOption.isDefined
  def canGoLeft: Boolean    = leftOption.isDefined
  def canGoRight: Boolean   = rightOption.isDefined

  def onForward(): Unit = channel.sendToGame(MapMessages.forward)
  def onLeft(): Unit    = channel.sendToGame(MapMessages.left)
  def onRight(): Unit   = channel.sendToGame(MapMessages.right)