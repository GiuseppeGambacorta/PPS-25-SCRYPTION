package org.scryption

import java.awt.Dimension
import scala.swing.*

object Main extends SimpleSwingApplication:

  def incrementCounter(counter: Int): Int = counter + 1

  override def top: MainFrame = new MainFrame:
    title = "App Multischermata ScalaSwing"
    preferredSize = new Dimension(600, 300)


    val centerContainer = new BoxPanel(Orientation.Vertical)


    def schermata1(): Panel = new FlowPanel:
      private var counter = 0
      val counterLabel = new Label(s"Contatore: $counter")
      val btn = new Button(Action("Cliccami!") {
        counter = incrementCounter(counter)
        counterLabel.text = s"Contatore: $counter"
      })
      contents += new Label("--- Schermata 1 ---")
      contents += counterLabel
      contents += btn

    def schermata2(): Panel = new FlowPanel:
      contents += new Label("--- Schermata 2 ---")
      contents += new Label("Contenuto personalizzato per la pagina 2")


    def cambiaVista(nuovaVista: Panel): Unit =
      centerContainer.contents.clear()
      centerContainer.contents += nuovaVista
      centerContainer.revalidate()
      centerContainer.repaint()

    cambiaVista(schermata1())


    contents = new BorderPanel:
      layout(new FlowPanel {
        contents += new Label("Navigazione:")
        contents += new Button(Action("Vai a Schermata 1") {
          cambiaVista(schermata1())
        })
        contents += new Button(Action("Vai a Schermata 2") {
          cambiaVista(schermata2())
        })
      }) = BorderPanel.Position.North

      layout(centerContainer) = BorderPanel.Position.Center