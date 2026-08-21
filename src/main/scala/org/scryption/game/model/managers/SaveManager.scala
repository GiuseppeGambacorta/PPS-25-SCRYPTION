package org.scryption.game.model.managers

import upickle.default.*
import org.scryption.GameEvents.*
import org.scryption.game.model.*
import org.scryption.game.model.items.*

import java.nio.file.{Files, Paths}

case class CardDTO(name: String, attack: Int, health: Int, isCreature: Boolean) derives ReadWriter
case class NodeDTO(eventName: String, next: Option[NodeDTO], left: Option[NodeDTO], right: Option[NodeDTO]) derives ReadWriter
case class SaveFileDTO(deck: List[CardDTO], inventory: List[String], map: NodeDTO) derives ReadWriter

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

  private def nodeToDTO(node: Node): NodeDTO =
    NodeDTO(
      eventName = eventToString.getOrElse(node.event, "fight"),
      next = node.nextNode.map(nodeToDTO),
      left = node.left.map(nodeToDTO),
      right = node.right.map(nodeToDTO)
    )

  private def dtoToNode(dto: NodeDTO): Node =
    Node(
      event = stringToEvent.getOrElse(dto.eventName, fight),
      nextNode = dto.next.map(dtoToNode),
      left = dto.left.map(dtoToNode),
      right = dto.right.map(dtoToNode)
    )

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

  def saveGame(state: GameState, map: GameMap, filePath: String = "savegame.json"): Unit =
    val deckDTO = state.deck.toList.map(cardToDTO)
    val itemsDTO = state.inventory.map(_.name)
    val mapDTO = nodeToDTO(map.Left)

    val saveData = SaveFileDTO(deckDTO, itemsDTO, mapDTO)
    val jsonString = write(saveData, indent = 2)

    Files.write(Paths.get(filePath), jsonString.getBytes)
    println("Game saved in " + filePath)

  def loadGame(filePath: String = "savegame.json"): Option[(GameState, GameMap)] =
    if !Files.exists(Paths.get(filePath)) then return None

    try
      val jsonString = new String(Files.readAllBytes(Paths.get(filePath)))
      val saveData = read[SaveFileDTO](jsonString)
      val loadedDeck = Deck.fromList(saveData.deck.map(dtoToCard))
      val loadedInventory = saveData.inventory.map(stringToItem)
      val loadedState = GameState(loadedDeck, loadedInventory, false)
      val loadedMap = GameMap(Nil, dtoToNode(saveData.map))
      Some((loadedState, loadedMap))
    catch
      case e: Exception =>
        println(s"Error during the loading: ${e.getMessage}")
        None
