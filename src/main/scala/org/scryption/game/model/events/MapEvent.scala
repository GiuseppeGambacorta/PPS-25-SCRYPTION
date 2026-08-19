package org.scryption.game.model.events

import org.scryption.{EventMessages, GUIChannelInterface}
import org.scryption.game.model.{Card, GameState}
import org.scryption.GameEvent



case class GameMap(ActualStep : Path, Done : Path, Left : Path)

enum Path:
  case Node (event: GameEvent, nextEvent : Path)
  case Bifurcation(Left : Node, Right : Node)
  case Nil

  def ::(event: GameEvent): Path = Path.Node(event, this)


object Path:

  extension (p: Path)
    def append(tail: Path): Path = p match
      case Path.Nil => tail
      case Path.Node(e, next) => Path.Node(e, next.append(tail))
      case Path.Bifurcation(left, right) => Path.Bifurcation(left.append(tail), right.append(tail))

def MapEvent(mapGame : Path, ch: GUIChannelInterface): Path = ???