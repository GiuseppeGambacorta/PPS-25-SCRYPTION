package org.scryption.game.model
import org.scryption.game.model.events.Event

enum Path:
  case Node(event: Event, next: Path)
  case Fork(left: Path, right: Path)
  case End

case class ActualMap(actualNode : Path.Node, map: Path)