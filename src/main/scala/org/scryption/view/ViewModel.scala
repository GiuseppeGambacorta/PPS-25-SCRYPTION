package org.scryption.view

import java.awt.Toolkit
import scala.swing.Dimension

class ViewModel {

  val screenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  val screenWidth: Int = screenSize.width
  val screenHeight: Int = screenSize.height

}
