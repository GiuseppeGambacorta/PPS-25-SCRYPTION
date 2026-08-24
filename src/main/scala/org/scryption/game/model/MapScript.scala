package org.scryption.game.model

sealed trait MapBranch[+E]

object MapBranch:
  case class Node[E](offset: Int, event: E) extends MapBranch[E]
  case class Fork[E](offset: Int, leftEvent: E, rightEvent: E) extends MapBranch[E]
  case class Join[E](offset: Int, event: E) extends MapBranch[E]

  def offset[E](branch: MapBranch[E]): Int = branch match
    case Node(offset, _)    => offset
    case Fork(offset, _, _) => offset
    case Join(offset, _)    => offset

class MapLevel[E](val branches: List[MapBranch[E]]):
  def :+(branch: MapBranch[E]): MapLevel[E] = new MapLevel(branches :+ branch)
  def nonEmpty: Boolean = branches.nonEmpty
  def head: MapBranch[E] = branches.head
  def tail: List[MapBranch[E]] = branches.tail

object MapLevel:
  def apply[E](branches: List[MapBranch[E]]): MapLevel[E] = new MapLevel(branches)

class MapScript[E](val levels: List[MapLevel[E]]):
  def :+(level: MapLevel[E]): MapScript[E] = new MapScript(levels :+ level)
  def isEmpty: Boolean = levels.isEmpty || levels.forall(!_.nonEmpty)
  def head: MapLevel[E] = levels.head
  def tail: List[MapLevel[E]] = levels.tail

object MapScript:
  def apply[E](levels: List[MapLevel[E]]): MapScript[E] = new MapScript(levels)
  def empty[E]: MapScript[E] = new MapScript(List.empty)
