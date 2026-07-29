package org.scryption.view

// All layout proportions for a card, derived from the width

final case class CardGeometry(cardWidth: Int) {

  private def pct(factor: Float): Int = Math.round(cardWidth * factor)

  val cardHeight: Int = pct(1.52f)

  val nameFontSize: Float = pct(0.16f).toFloat
  val statFontSize: Float = pct(0.24f).toFloat

  val portraitSize: Int = pct(0.77f)
  val costSize: Int = pct(0.4f)
  val borderSize: Int = pct(0.08f)

  val sigilSize: Int = pct(0.35f)
  val twinSigilSize: Int = pct(0.25f)

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

  val sigilLeftX: Int = pct(0.27f)
  val sigilLeftY: Int = pct(1.55f)

  val sigilRightX: Int = pct(0.5f)
  val sigilRightY: Int = pct(1.4f)
}
