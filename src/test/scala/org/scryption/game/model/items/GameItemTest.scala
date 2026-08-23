package org.scryption.game.model.items

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.{CardLibrary, Deck, PlayerHand}
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.events.FightState

class GameItemTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private def createInitialState(bottle: GameItem) =
    FightState(
      scalePoints = 0,
      bones = 0,
      deck = Deck.empty,
      playerHand = PlayerHand.empty,
      board = Board.empty,
      inventory = List(bottle)
    )

  Feature("Game Items"):

    Scenario("Using the Squirrel Bottle adds a Squirrel to the hand and removes the item from inventory"):
      Given("A FightState with 0 cards in hand and the bottle in inventory")
      val bottle = SquirrelBottle()
      val initialState: FightState = createInitialState(bottle)

      When("the player uses the Squirrel in a Bottle")
      val newState = bottle.use(initialState)

      Then("the player's hand contain exactly one card, which is a Squirrel")
      newState.playerHand.toList should have size 1
      newState.playerHand.toList.head.name shouldBe "Squirrel"

      And("the bottle is removed from the inventory")
      newState.inventory shouldBe empty

    Scenario("Hoggy Bank adds 4 bones and is removed from inventory"):
      Given("A FightState with 0 bones and the Hoggy Bank in inventory")
      val hoggyBank = HoggyBank()
      val initialState = createInitialState(hoggyBank)

      When("the player uses the Hoggy Bank")
      val newState = hoggyBank.use(initialState)

      Then("the player has exactly 4 bones")
      newState.bones shouldBe 4

      And("the Hoggy Bank is removed from the inventory")
      newState.inventory shouldBe empty

    Scenario("Pliers adds 1 point to the scale and is removed from inventory"):
      Given("A FightState with 0 scale points and Pliers in inventory")
      val pliers = Pliers()
      val initialState = createInitialState(pliers)

      When("the player uses the Pliers")
      val newState = pliers.use(initialState)

      Then("the scale points increases by 1")
      newState.scalePoints shouldBe 1
      And("the Pliers is removed from the inventory")
      newState.inventory shouldBe empty

    Scenario("Scissors destroys the targeted card and is removed from inventory"):

      Given("A FightState with an enemy card and Scissors in inventory")
      val scissors = Scissors()
      val initialState = createInitialState(scissors)
      val enemyRow: BoardRow = Some(CardLibrary.wolf) | x | x | x
      val boardWithEnemy = initialState.board.updateRow(IndexOfBotRow, enemyRow)
      val stateWithEnemy = initialState.copy(board = boardWithEnemy)

      When("the player uses the Scissors on the enemy's coordinates")
      val targetPos = (IndexOfBotRow, 0)
      val newState = scissors.use(stateWithEnemy, Some(targetPos))

      Then("the enemy card is destroyed (slot becomes None)")
      newState.board(targetPos._1)(targetPos._2) shouldBe None

      And("the Scissors are removed from the inventory")
      newState.inventory shouldBe empty

    Scenario("Scissors cannot destroy a player's own card"):
      Given("A FightState with a player card and Scissors in inventory")
      val scissors = Scissors()
      val initialState = createInitialState(scissors)
      val playerRow: BoardRow = Some(CardLibrary.wolf) | x | x | x
      val stateWithPlayerCard = initialState.copy(board = initialState.board.updateRow(IndexOfPlayerRow, playerRow))

      When("the player tries to use Scissors on their own card")
      val targetPos = (IndexOfPlayerRow, 0)
      val newState = scissors.use(stateWithPlayerCard, Some(targetPos))

      Then("the card remains on the board")
      newState.board(targetPos._1)(targetPos._2) shouldBe Some(CardLibrary.wolf)

      And("the Scissors is not consumed from the inventory")
      newState.inventory should contain(scissors)

    Scenario("Scissors cannot destroy a card in the bot's preparation row"):
      Given("A FightState with a card in the bot's preparation row (0) and Scissors in inventory")
      val scissors = Scissors()
      val initialState = createInitialState(scissors)

      val prepRow: BoardRow = Some(CardLibrary.wolf) | x | x | x
      val stateWithPrepCard = initialState.copy(board = initialState.board.updateRow(IndexOfBotPrepRow, prepRow))

      When("the player tries to use Scissors on the preparation row")
      val targetPos = (IndexOfBotPrepRow, 0)
      val newState = scissors.use(stateWithPrepCard, Some(targetPos))

      Then("the card remains on the board")
      newState.board(targetPos._1)(targetPos._2) shouldBe Some(CardLibrary.wolf)

      And("the Scissors is not consumed from the inventory")
      newState.inventory should contain(scissors)
