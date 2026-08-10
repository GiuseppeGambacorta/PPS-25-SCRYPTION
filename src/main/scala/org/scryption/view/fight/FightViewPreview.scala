package org.scryption.view.fight

import scala.swing.*
import java.awt.Dimension
import org.scryption.GUIChannel
import org.scryption.game.model.*
import org.scryption.game.model.boardModel.*

object FightViewPreview extends SimpleSwingApplication:

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Board View Sandbox"
    preferredSize = new Dimension(1280, 900)

    val dummyChannel = GUIChannel.getNewChannel
    val boardView = new BoardView(dummyChannel)

    // creating dummy cards
    val wolf = CreatureCard.empty named "Wolf" withAttack 3 withHealth 2 withSacrificeAttribute SacrificeAttribute.Blood(2)
    val squirrel = CreatureCard.empty named "Squirrel" withHealth 1
    val mantis = CreatureCard.empty named "Mantis God" withAttack 1 withHealth 1 addSeal Seal.TrifurcatedStrike

    // testing board
    val row0 = Some(wolf) | x | x | x
    val row1 = x | x | Some(mantis) | x
    val row2 = x | Some(squirrel) | x | x

    val dummyBoard = Board(row0, row1, row2)

    val fightView = new FightView(dummyChannel)

    fightView.boardView.updateBoard(dummyBoard)

    fightView.handView.updateHand(List(
      wolf, squirrel, mantis, wolf, squirrel, mantis, wolf
    ))
    
    contents = fightView