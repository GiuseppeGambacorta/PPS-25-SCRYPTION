package org.scryption

import org.scryption.view.common.GUIGeometry.StartScreenGeometry
import org.scryption.view.StartScreenView
import java.awt.{Dimension, Toolkit}
import scala.swing.*

object MainWindow extends SimpleSwingApplication:


  private val screenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Main Window"

    size = screenSize
    preferredSize = screenSize
    minimumSize = new Dimension(800, 600)


    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)

    val centerContainer = new BoxPanel(Orientation.Vertical) {
      preferredSize = screenSize
    }
    contents = centerContainer

    lazy val controller = new GameController(
      onViewChange = panel => changePanel(panel),
      onGameOver = () => changePanel(startScreen)
    )

    lazy val startScreen: StartScreenView = new StartScreenView(
      StartScreenGeometry(screenSize.width),
      onNewGame = () => controller.startNewGame(),
      onQuit = () => System.exit(0)
    )

    startScreen.preferredSize = screenSize
    centerContainer.contents += startScreen

    def changePanel(nuovaVista: Panel): Unit =
      Swing.onEDT {
        centerContainer.contents.clear()
        nuovaVista.preferredSize = screenSize
        centerContainer.contents += nuovaVista
        centerContainer.revalidate()
        centerContainer.repaint()
      }