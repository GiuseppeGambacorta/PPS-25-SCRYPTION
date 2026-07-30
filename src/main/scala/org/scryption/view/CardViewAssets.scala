package org.scryption.view

// Naming conventions for card asset files.

object CardViewAssets {

  private val costFolder = "costtextures"
  private val portraitFolder = "portraits"
  private val sigilFolder = "sigils"
  private val templateFolder = "cardtemplates"

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

  def sigilPath(sigilName: String): String = s"$sigilFolder/ability_${normalize(sigilName)}.png"
}
