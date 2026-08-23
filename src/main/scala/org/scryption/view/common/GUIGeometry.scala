package org.scryption.view.common

import scala.swing.Dimension

object GUIGeometry {
  final case class CardGeometry(cardWidth: Int) {

    private def pct(factor: Float): Int = Math.round(cardWidth * factor)

    val cardHeight: Int = pct(1.52f)

    val nameFontSize: Int = pct(0.16f)
    val statFontSize: Int = pct(0.24f)

    val portraitSize: Int = pct(0.77f)
    val costSize: Int = pct(0.4f)
    val borderSize: Int = pct(0.08f)

    val sigilSize: Int = pct(0.35f)
    val twinSigilSize: Int = pct(0.25f)

    val patchSize: Int = pct(0.4f)
    val addedSigilSize: Int = pct(0.25f)

    val portraitX: Int = pct(0.1f)
    val portraitY: Int = pct(0.26f)

    val costX: Int = pct(0.56f)
    val costY: Int = pct(0.2f)

    val nameY: Int = pct(0.2f)

    val attackX: Int = pct(0.1f)
    val attackY: Int = pct(1.32f)
    val healthX: Int = pct(0.8f)
    val healthY: Int = pct(1.38f)

    val sigilCenterX: Int = pct(0.35f)
    val sigilCenterY: Int = pct(1.07f)

    val patchCenterX: Int = pct(0.5f)
    val patchCenterY: Int = pct(1.25f)

    val sigilLeftX: Int = pct(0.27f)
    val sigilLeftY: Int = pct(1.55f)

    val sigilRightX: Int = pct(0.5f)
    val sigilRightY: Int = pct(1.4f)

    val addedSigilSlotCenters: Vector[(Int, Int)] = Vector(
      (pct(0.25f), pct(0.45f)),
      (pct(0.25f), pct(0.8f)),
      (pct(0.6f), pct(0.35f)),
      (pct(0.65f), pct(0.7f))
    )
  }

  final case class StartScreenGeometry(windowWidth: Int) {

    private def pct(factor: Float): Int = Math.round(windowWidth * factor)

    val part1Y: Int = pct(0.03375f)
    val part2Y: Int = pct(0.16875f)
    val part3Y: Int = pct(0.39375f)

    val slotWidth: Int = pct(0.125f)
    val slotHeight: Int = pct(0.16146f)

    val buttonWidth: Int = pct(0.109375f)
    val buttonHeight: Int = pct(0.14583f)

    val buttonGap: Int = pct(0.0417f)

    val textWidth: Int = pct(0.334375f)
    val textHeight: Int = pct(0.04375)
  }
}
