package org.scryption.view

import java.awt.Font
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import scala.util.Using

// Resolves classpath resources to filesystem paths and loads fonts/images.

object ResourceLoader {

  private def openResource(relativePath: String): Option[InputStream] = {
    val path = if (relativePath.startsWith("/")) relativePath else s"/$relativePath"
    Option(getClass.getResourceAsStream(path))
  }

  def loadFont(relativePath: String, size: Float): Font = {
    val loaded = openResource(relativePath).flatMap { stream =>
      Using(stream)(s => Font.createFont(Font.TRUETYPE_FONT, s)).toOption
    }
    loaded.map(_.deriveFont(size)).getOrElse(new Font("SansSerif", Font.BOLD, size.toInt))
  }

  /** Loads an image resource and scales it to a square of the given size. */
  def loadImage(relativePath: String, size: Int): Option[java.awt.Image] =
    openResource(relativePath)
      .flatMap(stream => Using(stream)(s => Option(ImageIO.read(s))).toOption)
      .flatten
      .map(_.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH))

  /** Loads a full-size template image (card front/back) with no scaling. */
  def loadTemplateImage(relativePath: String): Option[BufferedImage] =
    openResource(relativePath)
      .flatMap(stream => Using(stream)(s => Option(ImageIO.read(s))).toOption)
      .flatten
}
