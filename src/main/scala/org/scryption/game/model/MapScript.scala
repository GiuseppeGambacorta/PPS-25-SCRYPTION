package org.scryption.game.model

import scala.IArray.foreach

sealed trait MapBranch[+E]

object MapBranch:
  case class Node[E](event: E) extends MapBranch[E]
  case class Fork[E](leftEvent: E, rightEvent: E) extends MapBranch[E]
  case class Join[E](event: E) extends MapBranch[E]

class MapLevel[E](val branches: List[MapBranch[E]]):
  def :+(branch: MapBranch[E]): MapLevel[E] = new MapLevel(branches :+ branch)
  def isEmpty: Boolean = branches.isEmpty
  def head: MapBranch[E] = branches.head
  def tail: MapLevel[E] = MapLevel(branches.tail)

object MapLevel:
  def apply[E](branches: List[MapBranch[E]]): MapLevel[E] = new MapLevel(branches)

class MapScript[E](val levels: List[MapLevel[E]]):
  def :+(level: MapLevel[E]): MapScript[E] = new MapScript(levels :+ level)
  def isEmpty: Boolean = levels match {
    case level :: next => level.isEmpty && next.isEmpty
    case level => level.isEmpty
  }
  def head: MapLevel[E] = levels.head
  def tail: MapScript[E] = MapScript(levels.tail)

object MapScript:
  def apply[E](levels: List[MapLevel[E]]): MapScript[E] = new MapScript(levels)
