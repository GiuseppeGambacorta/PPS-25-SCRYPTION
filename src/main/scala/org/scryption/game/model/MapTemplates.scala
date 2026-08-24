package org.scryption.game.model

import org.scryption.GameEvents.*

import scala.util.Random

object MapTemplates:
  def newGameMap: MapScript[GameEvent] = template

  private val template: MapScript[GameEvent] =
    MapScript(List(
      MapLevel(List(
        MapBranch.Node(3, fight)
      )),
      MapLevel(List(
        MapBranch.Node(3, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Fork(3, sacrifice, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Fork(1, fight, getANewItem),
        MapBranch.Fork(5, getANewCard, trial)
      )),
      MapLevel(List(
        MapBranch.Node(0, getANewCard),
        MapBranch.Node(2, mycologists),
        MapBranch.Node(4, fight),
        MapBranch.Node(6, getANewItem)
      )),
      MapLevel(List(
        MapBranch.Join(1, trial),
        MapBranch.Join(5, trial)
      )),
      MapLevel(List(
        MapBranch.Join(3, fight)
      )),
      MapLevel(List(
        MapBranch.Node(3, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Fork(3, sacrifice, mycologists)
      )),
      MapLevel(List(
        MapBranch.Join(3, fight)
      ))
    ))
