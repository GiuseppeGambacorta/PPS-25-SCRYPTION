package org.scryption.game.model

import org.scryption.GameEvents.*
import org.scryption.game.model.Maps.Path

object MapTemplates:
  def newGameMap: Path[GameEvent] = Path.fromScript(
    MapScript(List(
      MapLevel(List(
        MapBranch.Node(3, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(3, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Fork(3, randomEvent, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(1, randomEvent),
        MapBranch.Node(5, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Fork(1, randomEvent, randomEvent),
        MapBranch.Fork(5, randomEvent, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Join(1, randomEvent),
        MapBranch.Node(4, randomEvent),
        MapBranch.Node(6, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Node(1, randomEvent),
        MapBranch.Join(5, randomEvent)
      )),
      MapLevel(List(
        MapBranch.Join(3, randomEvent)
      ))
    ))
  )