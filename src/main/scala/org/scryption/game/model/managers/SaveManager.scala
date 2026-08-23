package org.scryption.game.model.managers

import upickle.default.*
import org.scryption.GameEvents.*
import org.scryption.game.model.*
import org.scryption.game.model.items.*

import java.nio.file.{Files, Paths}

case class CardDTO(name: String, attack: Int, health: Int, isCreature: Boolean) derives ReadWriter
sealed trait MapBranchDTO derives ReadWriter
case class NodeBranchDTO(offset: Int, event: String) extends MapBranchDTO derives ReadWriter
case class ForkBranchDTO(offset: Int, leftEvent: String, rightEvent: String) extends MapBranchDTO derives ReadWriter
case class JoinBranchDTO(offset: Int, event: String) extends MapBranchDTO derives ReadWriter

case class MapLevelDTO(branches: List[MapBranchDTO]) derives ReadWriter
case class MapScriptDTO(levels: List[MapLevelDTO]) derives ReadWriter

case class SaveFileDTO(deck: List[CardDTO], inventory: List[String], map: MapScriptDTO) derives ReadWriter


object SaveManager:

  private def stringToItem(name: String): GameItem = name match
    case "Squirrel in a Bottle" => SquirrelBottle()
    case "Hoggy Bank" => HoggyBank()
    case "Pliers" => Pliers()
    case "Scissors" => Scissors()
    case _ => SquirrelBottle()

  private val eventToString: Map[GameEvent, String] = Map(
    fight -> "fight",
    getANewCard -> "getANewCard",
    fireCampAttack -> "fireCampAttack",
    fireCampHealth -> "fireCampHealth",
    mycologists -> "mycologists",
    sacrifice -> "sacrifice",
    getANewItem -> "getANewItem"
  )
  private val stringToEvent: Map[String, GameEvent] = eventToString.map(_.swap)

  private def branchToDTO(branch: MapBranch[GameEvent]): MapBranchDTO = branch match
    case MapBranch.Node(offset, event) =>
      NodeBranchDTO(offset, eventToString.getOrElse(event, "fight"))
    case MapBranch.Fork(offset, left, right) =>
      ForkBranchDTO(offset, eventToString.getOrElse(left, "fight"), eventToString.getOrElse(right, "fight"))
    case MapBranch.Join(offset, event) =>
      JoinBranchDTO(offset, eventToString.getOrElse(event, "fight"))

  private def dtoToBranch(dto: MapBranchDTO): MapBranch[GameEvent] = dto match
    case NodeBranchDTO(offset, event) =>
      MapBranch.Node(offset, stringToEvent.getOrElse(event, fight))
    case ForkBranchDTO(offset, left, right) =>
      MapBranch.Fork(offset, stringToEvent.getOrElse(left, fight), stringToEvent.getOrElse(right, fight))
    case JoinBranchDTO(offset, event) =>
      MapBranch.Join(offset, stringToEvent.getOrElse(event, fight))

  private def levelToDTO(level: MapLevel[GameEvent]): MapLevelDTO =
    MapLevelDTO(level.branches.map(branchToDTO))

  private def dtoToLevel(dto: MapLevelDTO): MapLevel[GameEvent] =
    MapLevel(dto.branches.map(dtoToBranch))

  private def scriptToDTO(script: MapScript[GameEvent]): MapScriptDTO =
    MapScriptDTO(script.levels.map(levelToDTO))

  private def dtoToScript(dto: MapScriptDTO): MapScript[GameEvent] =
    MapScript(dto.levels.map(dtoToLevel))

  private def cardToDTO(card: Card[?]): CardDTO = card match
    case c: CreatureCard => CardDTO(c.name, c.attack, c.health, isCreature = true)
    case s: SupportCard  => CardDTO(s.name, 0, s.health, isCreature = false)

  private def dtoToCard(dto: CardDTO): Card[?] =
    val baseCardOpt = CardLibrary.byName(dto.name)
    val baseCard = baseCardOpt.getOrElse(throw new Exception(s"Card not found: ${dto.name}"))
    if dto.isCreature then
      baseCard.asInstanceOf[CreatureCard].withAttack(dto.attack).withHealth(dto.health)
    else
      baseCard.asInstanceOf[SupportCard].withHealth(dto.health)

  /** Saves the current game state and map script.
   *
   * @param state The current game state.
   * @param map The map script left to traverse.
   * @param filePath The path in which the game will be saved.
   */
  def saveGame(state: GameState, map: MapScript[GameEvent], filePath: String = "savegame.json"): Unit =
    val deckDTO = state.deck.toList.map(cardToDTO)
    val itemsDTO = state.inventory.map(_.name)
    val mapDTO = scriptToDTO(map)

    val saveData = SaveFileDTO(deckDTO, itemsDTO, mapDTO)
    val jsonString = write(saveData, indent = 2)

    Files.write(Paths.get(filePath), jsonString.getBytes)
    println("Game saved in " + filePath)

  /** Loads the last saved game stored.
   *
   * @param filePath Where the save file is located.
   * @return if present, the last game state and the remaining map script.
   */
  def loadGame(filePath: String = "savegame.json"): Option[(GameState, MapScript[GameEvent])] =
    if !Files.exists(Paths.get(filePath)) then return None

    try
      val jsonString = new String(Files.readAllBytes(Paths.get(filePath)))
      val saveData = read[SaveFileDTO](jsonString)
      val loadedDeck = Deck.fromList(saveData.deck.map(dtoToCard))
      val loadedInventory = saveData.inventory.map(stringToItem)
      val loadedState = GameState(loadedDeck, loadedInventory, false)
      val loadedMap = dtoToScript(saveData.map)
      Some((loadedState, loadedMap))
    catch
      case e: Exception =>
        println(s"Error during the loading: ${e.getMessage}")
        None
