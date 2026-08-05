package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scryption.game.model.HitTarget.*
import org.scryption.game.model.boardModel.*

class FightResolverTest extends AnyFeatureSpec with GivenWhenThen with Matchers with ScalaCheckPropertyChecks:

  private val basicResolver: FightResolver = new BasicResolver()

  Feature("Basic Fight Resolution"):
    Scenario("Attacking an empty opposing slot"):
      Given("A wolf attacking from column 1 an empty opponent row")
      val wolf = CardLibrary.wolf
      val opponentRow: BoardRow = x | x | x | x

      When("resolving the targets")
      val targets = basicResolver.getTargets(1, wolf, opponentRow)

      Then("it should target the opponent directly")
      targets shouldBe List(Opponent)

    Scenario("Attacking a slot with an opposing card"):
      Given("A wolf attacking from column 2 with a stoat in front of it")
      val wolf = CardLibrary.wolf
      val stoat = CardLibrary.stoat
      val stoatSlot: Slot = Some(stoat)
      val opponentRow: BoardRow = x | x | stoatSlot | x

      When("resolving the target")
      val targets = basicResolver.getTargets(2, wolf, opponentRow)

      Then("it should target the opposing card at column 2")
      targets shouldBe List(OpposingCard(2))

  private val advancedResolver: FightResolver = new BasicResolver with AirborneResolver

  Feature("Airborne and Wall Seals Resolution"):
    Scenario("Airborne card flies over a normal creature"):
      Given("A sparrow (Airborne) attacking from column 0 with a stoat in front of it")
      val sparrow = CardLibrary.sparrow
      val stoat = CardLibrary.stoat
      val opponentRow: BoardRow = Some(stoat) | x | x | x

      When("resolving the target")
      val targets = advancedResolver.getTargets(0, sparrow, opponentRow)

      Then("it should ignore the stoat and hit the opponent directly")
      targets shouldBe List(Opponent)

    Scenario("Wall seal blocks an Airborne card"):
      Given("A sparrow (Airborne) attacking from column 1 with a boulder (Wall) in front of it")
      val sparrow = CardLibrary.sparrow
      val boulder = SupportCard.empty named "Boulder" withHealth 5 addSeal Seal.Wall
      val opponentRow: BoardRow = x | Some(boulder) | x | x

      When("resolving the target")
      val targets = advancedResolver.getTargets(1, sparrow, opponentRow)

      Then("it should be blocked and target the opposing boulder")
      targets shouldBe List(OpposingCard(1))