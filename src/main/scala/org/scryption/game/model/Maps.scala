package org.scryption.game.model
import org.scryption.game.model.MapScript

import scala.annotation.tailrec

object Maps:

  enum Path[E]:
    case Node(event: E, next: Path[E])
    case Fork(left: Path[E], right: Path[E])
    case End()

  object Path:

    def fromScript[E](script: MapScript[E]): Path[E] =
      if (!script.isEmpty) buildPath(3, script)
      else End()

    private def buildPath[E](offset: Int, script: MapScript[E]): Path[E] = script match {
      case levels: MapScript[E] if !levels.isEmpty => levels.head match {
        case level: MapLevel[E] if level.nonEmpty => findBranch(offset, level) match {
          case MapBranch.Node(o, event) => Node(event, buildPath(o, MapScript(levels.tail)))
          case MapBranch.Fork(o, left, right) => o match {
            case 3 => Fork(
              Node(left, buildPath(1, MapScript(levels.tail))),
              Node(right, buildPath(5, MapScript(levels.tail)))
            )
            case _ => Fork(
              Node(left, buildPath(o - 1, MapScript(levels.tail))),
              Node(right, buildPath(o + 1, MapScript(levels.tail)))
            )
          }
          case MapBranch.Join(o, event) => Node(event, buildPath(o, MapScript(levels.tail)))
        }
        case _ => End()
      }
      case _ => End()
    }

    @tailrec
    private def findBranch[E](offset: Int, level: MapLevel[E]): MapBranch[E] = level.head match {
      case MapBranch.Node(o, _) if o == offset => level.head
      case MapBranch.Fork(o, _, _) if o == offset => level.head
      case MapBranch.Join(o, _) => o match {
        case 1 if offset == 0 || offset == 2 => level.head
        case 3 if offset == 1 || offset == 5 => level.head
        case 5 if offset == 4 || offset == 6 => level.head
        case _ if level.tail.nonEmpty => findBranch(offset, MapLevel(level.tail))
      }
      case _ if level.tail.nonEmpty => findBranch(offset, MapLevel(level.tail))
    }
