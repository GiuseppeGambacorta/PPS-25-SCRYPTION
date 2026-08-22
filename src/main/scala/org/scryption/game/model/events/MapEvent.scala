package org.scryption.game.model.events

import org.scryption.GameEvents.GameEvent
import org.scryption.GameMessagesChannel
import org.scryption.MapMessages
import org.scryption.game.model.Maps.Path

import scala.annotation.tailrec

@tailrec
def MapEvent(gameMap: Path[GameEvent], ch: GameMessagesChannel): Path[GameEvent] =

  val msg = ch.receiveFromGui

  gameMap match
    case Path.Node(_, next) => next match
      case Path.Node(_, _) => msg match
        case MapMessages.forward => next
        case _ =>
          ch.clear()
          MapEvent(gameMap, ch)
      case Path.Fork(left, right) => msg match
        case MapMessages.left => left
        case MapMessages.right => right
        case _ =>
          ch.clear()
          MapEvent(gameMap, ch)
      case Path.End() => next
    case Path.End() => Path.End()
