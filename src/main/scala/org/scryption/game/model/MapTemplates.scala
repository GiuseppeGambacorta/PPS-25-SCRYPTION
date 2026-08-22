package org.scryption.game.model

import org.scryption.GameEvents.*

import scala.util.Random

object MapTemplates:
  def newGameMap: MapScript[GameEvent] = Random.shuffle(List(templateA, templateB, templateC, templateD)).head

  private val templateA: MapScript[GameEvent] =
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
        MapBranch.Join(3, fight)
      )),
      MapLevel(List(
        MapBranch.Fork(3, getANewCard, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Fork(1, getANewItem, randomEvent),
        MapBranch.Fork(5, fireCampHealth, sacrifice)
      )),
      MapLevel(List(
        MapBranch.Join(1, mycologists),
        MapBranch.Join(5, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Join(3, fight)
      )),
      MapLevel(List(
        MapBranch.Node(3, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Node(3, fight)
      ))
    ))

  private val templateB: MapScript[GameEvent] =
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
        MapBranch.Fork(5, getANewCard, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(0, getANewCard),
        MapBranch.Node(2, mycologists),
        MapBranch.Node(4, fight),
        MapBranch.Node(6, getANewItem)
      )),
      MapLevel(List(
        MapBranch.Join(1, randomEvent),
        MapBranch.Join(5, randomEvent)
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

  private val templateC: MapScript[GameEvent] =
    MapScript(List(
      MapLevel(List(
        MapBranch.Node(3, fight)
      )),
      MapLevel(List(
        MapBranch.Node(3, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Node(3, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Fork(3, fight, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Node(1, getANewItem),
        MapBranch.Fork(5, sacrifice, mycologists)
      )),
      MapLevel(List(
        MapBranch.Fork(1, fight, randomEvent),
        MapBranch.Join(5, fight)
      )),
      MapLevel(List(
        MapBranch.Node(0, fireCampHealth),
        MapBranch.Node(2, fight),
        MapBranch.Node(5, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Join(1, mycologists),
        MapBranch.Node(5, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Join(3, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(3, fight)
      ))
    ))

  private val templateD: MapScript[GameEvent] =
    MapScript(List(
      MapLevel(List(
        MapBranch.Node(3, fight)
      )),
      MapLevel(List(
        MapBranch.Node(3, getANewCard)
      )),
      MapLevel(List(
        MapBranch.Fork(3, fireCampAttack, getANewItem)
      )),
      MapLevel(List(
        MapBranch.Fork(1, getANewCard, randomEvent),
        MapBranch.Node(5, fight)
      )),
      MapLevel(List(
        MapBranch.Join(1, mycologists),
        MapBranch.Node(5, fireCampAttack)
      )),
      MapLevel(List(
        MapBranch.Node(1, fight),
        MapBranch.Node(5, mycologists)
      )),
      MapLevel(List(
        MapBranch.Node(1, getANewCard),
        MapBranch.Fork(5, sacrifice, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(1, sacrifice),
        MapBranch.Join(5, fight)
      )),
      MapLevel(List(
        MapBranch.Join(3, getANewItem)
      )),
      MapLevel(List(
        MapBranch.Node(3, fight)
      ))
    ))