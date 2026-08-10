package org.scryption.view.fight

import scala.swing.*
import java.awt.Dimension
import org.scryption.{GUIChannel, GUIChannelInterface}

object FightViewPreview extends SimpleSwingApplication:

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Fight View Sandbox"
    preferredSize = new Dimension(1280, 900)

    val dummyChannel: GUIChannelInterface = GUIChannel.getNewChannel
    val fightView = new BoardView(dummyChannel)
    contents = fightView