package org.scryption

import org.scryption.view.common.GUIGeometry.StartScreenGeometry
import org.scryption.view.StartScreenView
import java.awt.Dimension
import scala.swing.*

object MainWindow extends SimpleSwingApplication:

  override def top: MainFrame = new MainFrame:
    title = "Scryption - Main Window"

    minimumSize = new Dimension(800, 600)


    peer.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH)

    val centerContainer = new BorderPanel()
    contents = centerContainer

    lazy val controller = new GameController(
      onViewChange = panel => changePanel(panel),
      onGameOver = () => changePanel(startScreen)
    )

    lazy val startScreen: StartScreenView = new StartScreenView(
      StartScreenGeometry(1920),
      onNewGame = () => controller.startNewGame(),
      onQuit = () => System.exit(0)
    )

    centerContainer.layout(startScreen) = BorderPanel.Position.Center

    def changePanel(nuovaVista: Panel): Unit =
      Swing.onEDT {
        centerContainer.layout(nuovaVista) = BorderPanel.Position.Center
        centerContainer.revalidate()
        centerContainer.repaint()
      }