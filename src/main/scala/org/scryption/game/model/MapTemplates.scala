package org.scryption.game.model

import org.scryption.GameEvents.*
import org.scryption.game.model.Maps.Path

object MapTemplates:
  def newGameMap: Path[GameEvent] = Path.fromScript(
    MapScript(List(
      MapLevel(List(
        MapBranch.Node(3, randomEvent)
      ))
    ))
  )