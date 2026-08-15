package org.scryption.game.model.events

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.{Card, CreatureCard, Direction, GameState, Seal}
import org.scryption.game.model.Deck.*
import org.scryption.game.model.Seal.{MightyLeap, Sprinter}
import org.scryption.GUIMessages
import org.scryption.GUIChannel
import org.scryption.GUIChannelInterface

class ChangeDeckEventsTest extends AnyFeatureSpec with GivenWhenThen with Matchers:

  private val squirrel: CreatureCard = CreatureCard.empty withAttack 1 named ("squirrel") withHealth 1
  private val bear: CreatureCard = CreatureCard.empty withAttack 2 named ("bear") withHealth 3
  private val fox: CreatureCard = CreatureCard.empty withAttack 1 named ("fox") withHealth 1

  private val card1: CreatureCard = CreatureCard.empty withAttack  1 named ("firstCard") withHealth 1 addSeal Sprinter(Direction.Right) addSeal MightyLeap
  private val card2: CreatureCard = CreatureCard.empty withAttack  0 named ("secondCard") withHealth 1

  private def runMockGuiWithRetry(ch: GUIChannelInterface)(wrongResponses: List[GUIMessages], correctResponses: List[GUIMessages]): Thread =
    val thread = new Thread(() => {
      ch.receiveFromGame
      wrongResponses.foreach(ch.sendToGame)

      ch.receiveFromGame
      correctResponses.foreach(ch.sendToGame)
    })
    thread.start()
    thread

  Feature("GetANewCard event") {

    Scenario("Successfully adding a new card when receiving a valid SingleCard message") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(squirrel))

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)

      Then("The resulting deck should equal the initial deck with the chosen card added")
      updatedGameState.deck shouldBe (initialDeck addCard squirrel)
      updatedGameState.deck.size shouldBe initialDeck.size + 1

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Handling an unexpected Cards message before receiving a valid SingleCard") {
      Given("A mock GUI thread that sends Cards first, and SingleCard after retry")
      val initialDeck = fromList(squirrel :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.Cards(List(fox))),
        correctResponses = List(GUIMessages.SingleCard(squirrel))
      )

      When("Executing the GetANewCard event")
      val updatedGameState = getANewCard(initialGameState, ch)
      guiThread.join()

      Then("It should recover from the invalid message, clear the channel, and apply the valid card")
      updatedGameState.deck.size shouldBe initialDeck.size + 1
      updatedGameState.deck.toList should contain(squirrel)
    }
  }

  Feature("Firecamp Attack event") {

    Scenario("Powering up a card attack when receiving a valid SingleCard message") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new attack, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head

      updatedCard match {
        case updatedCreature: CreatureCard =>
          updatedCreature.attack shouldNot be(bear.attack)
          updatedCreature.withAttack(bear.attack) shouldBe bear

        case _ =>
          fail("The updated card is not a CreatureCard")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Handling an unexpected End message before receiving a valid SingleCard") {
      Given("A mock GUI thread that sends End first, and SingleCard after retry")
      val initialDeck = fromList(bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.End),
        correctResponses = List(GUIMessages.SingleCard(bear))
      )

      When("Executing the Firecamp Attack event")
      val updatedGameState = fireCamp_Attack(initialGameState, ch)
      guiThread.join()

      Then("The event should recover and process the valid card choice")
      updatedGameState.deck.size shouldBe initialDeck.size
      val updatedCard = updatedGameState.deck.toList.head
      updatedCard.asInstanceOf[CreatureCard].attack shouldBe (bear.attack + 1)
    }
  }

  Feature("Firecamp Health event") {

    Scenario("Powering up a card health when receiving a valid SingleCard message") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(squirrel :: bear :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Firecamp Health event")
      val updatedGameState = fireCamp_Health(initialGameState, ch)

      Then("The deck size should remain unchanged")
      updatedGameState.deck.size shouldBe initialDeck.size

      And("The selected card should be modified with new health, but all other attributes must remain identical")
      val updatedCard = (updatedGameState.deck.toList diff initialDeck.toList).head
      updatedCard.health shouldNot be(bear.health)
      updatedCard.withHealth(bear.health) shouldBe bear

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }
  }

  Feature("Mushroom Expert event") {

    Scenario("Returning the same GameState when no duplicate cards exist in the deck") {
      Given("An initial GameState with a deck containing no duplicates")
      val initialDeck = fromList(squirrel :: bear :: fox :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      When("Executing the Mushroom Expert event")
      val updatedGameState = mushRoomsExpert(initialGameState, ch)

      Then("The returned GameState should be identical to the initial GameState")
      updatedGameState shouldBe initialGameState
    }

    Scenario("Fusing duplicate cards when duplicate cards exist in the deck") {
      Given("An initial GameState with a deck containing duplicates")
      val initialDeck = fromList(bear :: bear :: squirrel :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      // Simula la risposta della GUI selezionando la carta duplicata (bear)
      ch.sendToGame(GUIMessages.SingleCard(bear))

      When("Executing the Mushroom Expert event")
      val updatedGameState = mushRoomsExpert(initialGameState, ch)

      Then("The deck size should decrease by 1 (2 duplicates removed, 1 fused card added)")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)

      And("The duplicate cards should be removed and replaced with a card with doubled stats")
      updatedGameState.deck.toList should not contain bear

      val fusedCard = updatedGameState.deck.toList.find(_.name == bear.name)

      fusedCard match {
        case Some(creature: CreatureCard) =>
          creature.attack shouldBe (bear.attack * 2)
          creature.health shouldBe (bear.health * 2)

        case _ =>
          fail("Expected a fused CreatureCard with updated stats")
      }

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }
  }

  Feature("Sacrifice event") {

    Scenario("Transferring seals from donor card to target card when receiving valid messages") {
      Given("An initial GameState with 2 cards and a GUIChannel")
      val initialDeck = fromList(card1 :: card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      ch.sendToGame(GUIMessages.SingleCard(card1))
      ch.sendToGame(GUIMessages.SingleCard(card2))

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)

      Then("The deck size should have been changed")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)

      And("The selected cards should be removed and the new card should have the seals of the first card")
      updatedGameState.deck.toList should not contain card1
      updatedGameState.deck.toList should not contain card2

      val updatedCard = updatedGameState.deck.toList.find(_.name == card2.name)

      updatedCard.get.asInstanceOf[CreatureCard].seals shouldBe card1.seals

      And("The game should not be over")
      updatedGameState.isGameOver shouldBe false
    }

    Scenario("Handling an invalid message sequence before receiving a valid pair of SingleCards") {
      Given("A mock GUI thread that sends SingleCard+Cards first, and two SingleCards after retry")
      val initialDeck = fromList(card1 :: card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = runMockGuiWithRetry(ch)(
        wrongResponses = List(GUIMessages.SingleCard(card1), GUIMessages.Cards(List(squirrel))),
        correctResponses = List(GUIMessages.SingleCard(card1), GUIMessages.SingleCard(card2))
      )

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)
      guiThread.join()

      Then("The sacrifice event should recover after the retry and combine the seals correctly")
      updatedGameState.deck.size shouldBe (initialDeck.size - 1)
      val updatedCard = updatedGameState.deck.toList.find(_.name == card2.name)
      updatedCard.get.asInstanceOf[CreatureCard].seals shouldBe card1.seals
    }

    Scenario("Returning the same GameState when no cards in the deck have any seals") {
      Given("An initial GameState where no card has seals")
      val initialDeck = fromList(squirrel :: bear :: card2 :: Nil)
      val initialGameState = GameState(initialDeck, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      When("Executing the Sacrifice event")
      val updatedGameState = sacrifice(initialGameState, ch)

      Then("The returned GameState should be identical to the initial GameState")
      updatedGameState shouldBe initialGameState
    }

    Scenario("Ensuring only cards with seals are offered for sacrifice") {
      Given("A deck containing cards with and without seals")
      val deckWithMixedCards = fromList(squirrel :: bear :: card1 :: Nil)
      val initialGameState = GameState(deckWithMixedCards, isGameOver = false)
      val ch = GUIChannel.getNewChannel

      val guiThread = new Thread(() => {
        val receivedMsg = ch.receiveFromGame
        receivedMsg match {
          case GUIMessages.Cards(offeredCards) =>
            offeredCards.forall(_.seals.nonEmpty) shouldBe true
            offeredCards should contain(card1)
            offeredCards should not contain squirrel
            offeredCards should not contain bear

            ch.sendToGame(GUIMessages.SingleCard(card1))

            ch.receiveFromGame
            ch.sendToGame(GUIMessages.SingleCard(bear))

          case _ =>
            fail("Expected GUIMessages.Cards from game")
        }
      })
      guiThread.start()

      When("Executing the Sacrifice event")
      sacrifice(initialGameState, ch)
      guiThread.join()
    }
  }