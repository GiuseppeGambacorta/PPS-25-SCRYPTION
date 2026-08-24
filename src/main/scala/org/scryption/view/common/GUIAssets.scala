package org.scryption.view.common

// Naming conventions for card asset files.

object GUIAssets {

  final case class CardViewAssets() {

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

    def backTemplatePath: String = s"$templateFolder/back.png"

    def costIconPath(costCode: String): String = s"$costFolder/cost_$costCode.png"

    def portraitPath(name: String): String = s"$portraitFolder/portrait_${normalize(name)}.png"

    def sigilPath(sigilName: String): String = s"$sigilFolder/sigil_${normalize(sigilName)}.png"

    def sigilPatchPath: String = s"$sigilFolder/sigil_patch.png"

    def fontPath: String = s"$fontFolder/HEAVYWEI.TTF"

    def emissionPath(name: String): String = s"$portraitFolder/portrait_${normalize(name)}_emission.png"

    def slotPath(name: String): String = s"$slotFolder/slot_${normalize(name)}.png"
  }

  final case class StartScreenViewAssets() {
    private val menuFolder = "menu"

    def startScreenPath: String = s"$menuFolder/startscreen.png"

    def menuBackgroundPath: String = s"$menuFolder/startscreen_background_PART1.png"

    def menuSlotPath: String = s"$menuFolder/startscreen_slot_PART1.png"

    def menuHighlightedSlotPath: String = s"$menuFolder/startscreen_slot_highlighted_PART1.png"

    def menuCardPath(name: String): String = s"$menuFolder/menucard_$name.png"

    def menuTextPath(name: String): String = s"$menuFolder/menutext_$name.png"
  }

  final case class MapViewAssets() {
    private val mapFolder = "map"

    def eventIconPath(name: String): String = s"$mapFolder/$name.png"
  }
}
