package org.scryption.game.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scryption.game.model.MapBranch.*
import org.scryption.game.model.Maps.Path
import org.scryption.GameEvents.*

class MapTests extends AnyFeatureSpec with GivenWhenThen with Matchers:

  Feature("Creation of a MapScript") {

    Scenario("The MapScript must be empty if there's no levels") {
      Given("an empty MapLevel List")
      val levels: List[MapLevel[GameEvent]] = List.empty

      When("creating a new MapScript")
      val script: MapScript[GameEvent] = MapScript[GameEvent](levels)

      Then("The result should be empty")
      script.isEmpty.shouldBe(true)
    }

    Scenario("The MapScript must be empty if there's no steps") {
      Given("a MapLevel List with an empty MapStep List")
      val levels: List[MapLevel[GameEvent]] = List(MapLevel(List.empty))

      When("creating a new MapScript")
      val script: MapScript[GameEvent] = MapScript[GameEvent](levels)

      Then("The result should be empty")
      script.isEmpty.shouldBe(true)
    }

    Scenario("The MapScript must not be empty") {
      Given("a MapLevel List with a MapStep List with an GameEvent")
      val levels: List[MapLevel[GameEvent]] = List(MapLevel(List(Node(3, randomEvent))))

      When("creating a new MapScript")
      val script: MapScript[GameEvent] = MapScript[GameEvent](levels)

      Then("The result should be a Node")
      script.isEmpty.shouldBe(false)
    }
  }

  Feature("Creation of a Path from a MapScript") {

    Scenario("The Path must be End") {

      Given("an empty MapScript")
      val script: MapScript[GameEvent] = MapScript(List.empty)

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must be End")
      gameMap.shouldBe(Path.End())
    }

    Scenario("The Path must be a Node") {

      Given("a MapScript with an event")
      val event = randomEvent

      val script: MapScript[GameEvent] = MapScript(List(MapLevel(List(Node(3, event)))))

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must be a Node")
      gameMap.shouldBe(Path.Node(event, Path.End()))
    }

    Scenario("The Path must be a sequence of Nodes") {

      Given("a MapScript with a few linear events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Node(3, event2)
            )
          ),
          MapLevel(
            List(
              Node(3, event3)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must be a sequence of Node")
      gameMap.shouldBe(Path.Node(event1, Path.Node(event2, Path.Node(event3, Path.End()))))
    }

    Scenario("The Path must have a two branches") {

      Given("a MapScript with a few events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must have two branches")
      gameMap.shouldBe(Path.Node(event1, Path.Fork(Path.Node(event2, Path.End()), Path.Node(event3, Path.End()))))
    }

    Scenario("The Path must have a two longer branches") {

      Given("a MapScript with a few events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent
      val event4 = randomEvent
      val event5 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          ),
          MapLevel(
            List(
              Node(1, event4),
              Node(5, event5)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must have two longer branches")
      gameMap.shouldBe(
        Path.Node(
          event1,
          Path.Fork(
            Path.Node(event2, Path.Node(event4, Path.End())),
            Path.Node(event3, Path.Node(event5, Path.End()))
          )
        )
      )
    }

    Scenario("The Path must have a two branches that join back") {

      Given("a MapScript with a few events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent
      val event4 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          ),
          MapLevel(
            List(
              Join(3, event4)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must have two branches")
      gameMap.shouldBe(
        Path.Node(
          event1,
          Path.Fork(
            Path.Node(event2, Path.Node(event4, Path.End())),
            Path.Node(event3, Path.Node(event4, Path.End()))
          )
        )
      )
    }

    Scenario("The Path must have three branches") {

      Given("a MapScript with a few events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent
      val event4 = randomEvent
      val event5 = randomEvent
      val event6 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          ),
          MapLevel(
            List(
              Node(1, event4),
              Fork(5, event5, event6)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must have three branches")
      gameMap.shouldBe(
        Path.Node(
          event1,
          Path.Fork(
            Path.Node(event2, Path.Node(event4, Path.End())),
            Path.Node(
              event3,
              Path.Fork(
                Path.Node(event5, Path.End()),
                Path.Node(event6, Path.End())
              )
            )
          )
        )
      )
    }

    Scenario("The Path must have a four branches that join back") {

      Given("a MapScript with some events")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent
      val event4 = randomEvent
      val event5 = randomEvent
      val event6 = randomEvent
      val event7 = randomEvent
      val event8 = randomEvent
      val event9 = randomEvent
      val event10 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          ),
          MapLevel(
            List(
              Fork(1, event4, event5),
              Fork(5, event6, event7)
            )
          ),
          MapLevel(
            List(
              Join(1, event8),
              Join(5, event9)
            )
          ),
          MapLevel(
            List(
              Join(3, event10)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result must have four branches")
      gameMap.shouldBe(
        Path.Node(
          event1,
          Path.Fork(
            Path.Node(
              event2,
              Path.Fork(
                Path.Node(event4, Path.Node(event8, Path.Node(event10, Path.End()))),
                Path.Node(event5, Path.Node(event8, Path.Node(event10, Path.End())))
              )
            ),
            Path.Node(
              event3,
              Path.Fork(
                Path.Node(event6, Path.Node(event9, Path.Node(event10, Path.End()))),
                Path.Node(event7, Path.Node(event9, Path.Node(event10, Path.End())))
              )
            )
          )
        )
      )
    }

    Scenario("The Path must have expected structure") {

      Given("some events and a MapScript")
      val event1 = randomEvent
      val event2 = randomEvent
      val event3 = randomEvent
      val event4 = randomEvent
      val event5 = randomEvent
      val event6 = randomEvent
      val event7 = randomEvent
      val event8 = randomEvent
      val event9 = randomEvent

      val script: MapScript[GameEvent] = MapScript(
        List(
          MapLevel(
            List(
              Node(3, event1)
            )
          ),
          MapLevel(
            List(
              Fork(3, event2, event3)
            )
          ),
          MapLevel(
            List(
              Node(1, event4),
              Fork(5, event5, event6)
            )
          ),
          MapLevel(
            List(
              Node(1, event7),
              Join(5, event8)
            )
          ),
          MapLevel(
            List(
              Join(3, event9)
            )
          )
        )
      )

      When("creating a new Path")
      val gameMap: Path[GameEvent] = Path.fromScript(script)

      Then("The result should be equal")
      gameMap.shouldBe(
        Path.Node(
          event1,
          Path.Fork(
            Path.Node(event2, Path.Node(event4, Path.Node(event7, Path.Node(event9, Path.End())))),
            Path.Node(
              event3,
              Path.Fork(
                Path.Node(event5, Path.Node(event8, Path.Node(event9, Path.End()))),
                Path.Node(event6, Path.Node(event8, Path.Node(event9, Path.End())))
              )
            )
          )
        )
      )
    }
  }
