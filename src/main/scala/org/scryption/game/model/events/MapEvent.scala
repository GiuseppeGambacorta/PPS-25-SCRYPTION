package org.scryption.game.model.events

import org.scryption.GameMessagesChannel
import org.scryption.MapMessages
import org.scryption.game.model.GameMap

import scala.annotation.tailrec


@tailrec
def MapEvent(gameMap: GameMap, ch: GameMessagesChannel): GameMap =
  val current = gameMap.Left

  val msg = ch.receiveFromGui
  msg match
    case MapMessages.forward if current.nextNode.isDefined =>
      GameMap(gameMap.Done :+ current, current.nextNode.get)

    case MapMessages.left if current.left.isDefined =>
      GameMap(gameMap.Done :+ current, current.left.get)

    case MapMessages.right if current.right.isDefined =>
      GameMap(gameMap.Done :+ current, current.right.get)

    case _ =>
      ch.clear()
      MapEvent(gameMap, ch)

