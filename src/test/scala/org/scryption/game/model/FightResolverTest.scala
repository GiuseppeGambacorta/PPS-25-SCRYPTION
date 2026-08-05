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