package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.managers.HitTarget.*
import org.scryption.game.model.boardModel.*
import org.scryption.game.model.managers.{AirborneResolver, BasicResolver, FightResolver, StrikeResolver}

class FightResolverTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val basicResolver: FightResolver = new BasicResolver()

  private val wolf = CardLibrary.wolf
  private val stoat = CardLibrary.stoat
  private val sparrow = CardLibrary.sparrow
  private val boulder = SupportCard.empty named "Boulder" withHealth 5 addSeal Seal.MightyLeap
  private val mantis = CardLibrary.mantis
  private val mantisGod = CreatureCard.empty named "MantisGod" withAttack 1 withHealth 1 addSeal Seal.TrifurcatedStrike

  Feature("Basic Fight Resolution"):
    Scenario("Attacking an empty opposing slot"):
      Given("A wolf attacking from column 1 an empty opponent row")
      val opponentRow: BoardRow = x | x | x | x

      When("resolving the targets")
      val targets = basicResolver.getTargets(1, wolf, opponentRow)

      Then("it should target the opponent directly")
      targets shouldBe List(Opponent)

    Scenario("Attacking a slot with an opposing card"):
      Given("A wolf attacking from column 2 with a stoat in front of it")
      val stoatSlot: Slot = Some(stoat)
      val opponentRow: BoardRow = x | x | stoatSlot | x

      When("resolving the target")
      val targets = basicResolver.getTargets(2, wolf, opponentRow)

      Then("it should target the opposing card at column 2")
      targets shouldBe List(OpposingCard(2))

  private val advancedResolver: FightResolver = new BasicResolver with AirborneResolver with StrikeResolver

  Feature("Airborne and Wall Seals Resolution"):
    Scenario("Airborne card flies over a normal creature"):
      Given("A sparrow (Airborne) attacking from column 0 with a stoat in front of it")
      val opponentRow: BoardRow = Some(stoat) | x | x | x

      When("resolving the target")
      val targets = advancedResolver.getTargets(0, sparrow, opponentRow)

      Then("it should ignore the stoat and hit the opponent directly")
      targets shouldBe List(Opponent)

    Scenario("Wall seal blocks an Airborne card"):
      Given("A sparrow (Airborne) attacking from column 1 with a boulder (Wall) in front of it")
      val opponentRow: BoardRow = x | Some(boulder) | x | x

      When("resolving the target")
      val targets = advancedResolver.getTargets(1, sparrow, opponentRow)

      Then("it should be blocked and target the opposing boulder")
      targets shouldBe List(OpposingCard(1))

  Feature("Strike Seals Resolution"):
    Scenario("Bifurcated Strike attacks left and right"):
      Given("A mantis (BifurcatedStrike) at column 1 facing an empty row")
      val opponentRow: BoardRow = x | x | x | x

      When("resolving targets")
      val targets = advancedResolver.getTargets(1, mantis, opponentRow)

      Then("it should hit column 0 and 2 (Opponent directly)")
      targets should contain theSameElementsAs List(Opponent, Opponent)

    Scenario("Trifurcated Strike attacks left, center, right and respects boundaries"):
      Given("A mantis god (TrifurcatedStrike) at column 3 (right edge)")
      val opponentRow: BoardRow = x | x | x | x

      When("resolving targets")
      val targets = advancedResolver.getTargets(3, mantisGod, opponentRow)

      Then("it should hit column 2 and 3, dropping the out-of-bounds right strike")
      targets should contain theSameElementsAs List(Opponent, Opponent)