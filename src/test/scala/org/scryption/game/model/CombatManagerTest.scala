package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.boardModel.*

class CombatManagerTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val resolver: FightResolver = new BasicResolver with AirborneResolver with StrikeResolver

  private val wolf = CardLibrary.wolf
  private val opossum = CardLibrary.opossum
  private val grizzly = CardLibrary.grizzly
  private val stoat = CardLibrary.stoat
  private val adder = CardLibrary.adder
  private val mantis = CardLibrary.mantis
  private val cockroach = CardLibrary.cockroach
  private val coyote = CardLibrary.coyote
  private val boneKingCreature = CreatureCard.empty withAttack 1 named "boneKing" withHealth 1 addSeal Seal.BoneKing

  Feature("Combat execution and damage application"):
    Scenario("Attacking an empty slot deals direct damage to the opponent"):
      Given("A wolf attacking an empty opposing row")
      val opponentRow: BoardRow = x | x | x | x

      When("the attack is executed")
      val result = CombatManager.executeAttack(1, wolf, opponentRow, resolver)

      Then("it should deal 3 damage to the opponent")
      result.damageToOpponent shouldBe 3

      And("the opposing row should remain unchanged")
      result.updatedRow shouldBe opponentRow
      result.killedCards.isEmpty shouldBe true

    Scenario("Attacking a creature reduces its health without killing it"):
      Given("An opossum attacking a grizzly")
      val opponentRow: BoardRow = Some(grizzly) | x | x | x

      When("the attack is executed")
      val result = CombatManager.executeAttack(0, opossum, opponentRow, resolver)

      Then("the opponent should take 0 damage")
      result.damageToOpponent shouldBe 0

      And("the grizzly's health should be reduced by 1")
      val updatedCard = result.updatedRow(0).get
      updatedCard.health shouldBe 5
      result.killedCards.isEmpty shouldBe true

    Scenario("Attacking a creature with lethal damage removes it from the board"):
      Given("A wolf attacking a stoat")
      val opponentRow: BoardRow = x | Some(stoat) | x | x

      When("the attack is executed")
      val result = CombatManager.executeAttack(1, wolf, opponentRow, resolver)

      Then("the stoat should be removed from the board slot")
      result.updatedRow(1) shouldBe x

      And("the stoat should be added to the killedCards list")
      result.killedCards should contain allElementsOf List(stoat)

    Scenario("Touch of Death instantly kills a creature regardless of health"):
      Given("An adder (with Touch of Death) attacking a grizzly")
      val opponentRow: BoardRow = x | x | Some(grizzly) | x

      When("the attack is executed")
      val result = CombatManager.executeAttack(2, adder, opponentRow, resolver)

      Then("the grizzly should be instantly removed from the slot")
      result.updatedRow(2) shouldBe x

      And("the grizzly should be added to the killedCards list")
      result.killedCards should contain allElementsOf List(grizzly)

    Scenario("Bifurcated Strike attacks multiple targets accumulating damage and row updates"):
      Given("A mantis (with Bifurcated Strike) attacking from column 1")
      And("an opponent row with a stoat at column 0 and empty space at column 2")
      val opponentRow: BoardRow = Some(stoat) | x | x | x

      When("the attack is executed")
      val result = CombatManager.executeAttack(1, mantis, opponentRow, resolver)

      Then("the opponent should take 1 damage from the right strike at empty column 2")
      result.damageToOpponent shouldBe 1

      And("the stoat at column 0 should take 1 damage from the left strike")
      val updatedStoat = result.updatedRow(0).get
      updatedStoat.health shouldBe 2

      And("the rest of the row should remain unchanged")
      result.updatedRow(1) shouldBe x
      result.updatedRow(2) shouldBe x
      result.updatedRow(3) shouldBe x
      result.killedCards.isEmpty shouldBe true

  Feature("Full row attack execution and death seals"):
    Scenario("A normal card dying gives 1 bone"):
      Given("An attacker row with a wolf and a defender row with a coyote")
      val attackerRow: BoardRow = Some(wolf) | x | x | x
      val defenderRow: BoardRow = Some(coyote) | x | x | x

      When("the full row attack is executed")
      val result = CombatManager.executeRowAttack(attackerRow, defenderRow, resolver)

      Then("the coyote should be removed")
      result.updatedOpponentRow(0) shouldBe x

      And("it should give exactly 1 bone")
      result.earnedBones shouldBe 1

      And("no cards should be returned to hand")
      result.returnedToHandCards shouldBe empty

    Scenario("Seal: BoneKing gives 4 bones upon death"):
      Given("An attacker row with a wolf and a defender row with a BoneKing creature")
      val attackerRow: BoardRow = Some(wolf) | x | x | x
      val defenderRow: BoardRow = Some(boneKingCreature) | x | x | x

      When("the full row attack is executed")
      val result = CombatManager.executeRowAttack(attackerRow, defenderRow, resolver)

      Then("the BoneKing creature should be removed")
      result.updatedOpponentRow(0) shouldBe x

      And("it should give exactly 4 bones thanks to the seal")
      result.earnedBones shouldBe 4
