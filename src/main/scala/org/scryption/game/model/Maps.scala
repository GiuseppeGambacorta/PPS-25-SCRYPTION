package org.scryption.game.model
import org.scryption.game.model.events.Event

object Maps:

  enum Path[E]:
    case Node(event: E, next: Path[E])
    case Fork(left: Path[E], right: Path[E])
    case End()

  object Path:

    def createFromList(events: List[Event]): Path[Event] = events match {
      case empty => End()
    }


  //case class ActualMap(actualNode : Path.Node, map: Path)