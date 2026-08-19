package org.scryption.game.model
import org.scryption.game.model.MapScript

object Maps:

  enum Path[E]:
    case Node(event: E, next: Path[E])
    case Fork(left: Path[E], right: Path[E])
    case End()

  object Path:

    def fromScript[E](script: MapScript[E]): Path[E] = script match {
      case levels => levels.head match {
        case level: MapLevel[E] => level.head match {
          case branch: MapBranch[E] => branch match {
            case MapBranch.Node(event)       => levels.tail match {
              case nextLevels if !nextLevels.isEmpty => Node(event, fromScript(nextLevels))
              case _                                 => Node(event, End())
            }
            case MapBranch.Fork(left, right) => levels.tail match {
              case nextLevels if !nextLevels.isEmpty => nextLevels.head
                Fork(Node(left, fromScript(nextLevels)), Node(right, fromScript(nextLevels)))
              case _                                 => Fork(Node(left, End()), Node(right, End()))
            }
            case MapBranch.Join(event)       => levels.tail match {
              case nextLevels if !nextLevels.isEmpty => Node(event, fromScript(nextLevels))
              case _                                 => Node(event, End())
            }
          }
          // TO-DO: next branches
        }
      }
    }