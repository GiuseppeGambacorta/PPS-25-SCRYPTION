package org.scryption.view

// Naming conventions for card asset files.

object CardViewAssets {

  private val costFolder = "costs"
  private val portraitFolder = "portraits"
  private val sigilFolder = "sigils"
  private val templateFolder = "cardtemplates"
  private val fontFolder = "heavyweight-cufonfonts"
  private val slotFolder = "slots"

  private val typeTemplates: Map[String, String] = Map(
    "rare" -> "rare_front.png",
    "gold" -> "gold_front.png"
  )

  private def normalize(name: String): String = name.toLowerCase.replace(" ", "_")

  def frontTemplatePath(cardType: String): String =
    typeTemplates
      .get(cardType.toLowerCase)
      .map(fileName => s"$templateFolder/$fileName")
      .getOrElse(s"$templateFolder/front.png")

  val backTemplatePath: String = s"$templateFolder/back.png"

  def costIconPath(costCode: String): String = s"$costFolder/cost_$costCode.png"

  def portraitPath(name: String): String = s"$portraitFolder/portrait_${normalize(name)}.png"

  def sigilPath(sigilName: String): String = s"$sigilFolder/sigil_${normalize(sigilName)}.png"

  def sigilPatchPath: String = s"$sigilFolder/sigil_patch.png"

  def fontPath: String = s"$fontFolder/HEAVYWEI.TTF"

  def emissionPath(name: String): String = s"$portraitFolder/portrait_${normalize(name)}_emission.png"

  def slotPath(name: String): String = s"$slotFolder/slot_${normalize(name)}.png"
}
