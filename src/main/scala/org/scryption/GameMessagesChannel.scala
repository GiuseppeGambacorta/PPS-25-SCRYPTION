package org.scryption

import org.scryption.game.model.BoardPosition
import org.scryption.game.model.Card
import org.scryption.game.model.events.FightState
import org.scryption.game.model.events.TurnState
import org.scryption.game.model.items.GameItem

import java.util.concurrent.LinkedBlockingQueue

/** Common supertype for every message exchanged on the [[GameMessagesChannel]]. Being `sealed`, the compiler knows the
  * closed set of its direct subtypes, which enables exhaustive pattern matching wherever a message is handled.
  */
sealed trait GameMessage

/** The kind of trial the player can choose during a trial event. */
enum Trial:
  case Health
  case Attack
  case Seals

/** Messages for generic single-interaction events (e.g. card or item selection). */
enum EventMessages extends GameMessage:
  /** A batch of cards offered to the player to choose from. */
  case Cards(cards: List[Card[?]])

  /** A single card selected by the player. */
  case SingleCard(card: Card[?])

  /** The trial chosen by the player. */
  case TrialChoice(trial: Trial)

  /** A batch of items offered to the player to choose from. */
  case Items(items: List[GameItem])

  /** A single item selected by the player. */
  case SingleItem(item: GameItem)

  /** Signals the end of the event. */
  case End

/** Messages dedicated to combat, covering both player actions and turn-state notifications. */
enum FightMessages extends GameMessage:
  /** Notifies the view of the current fight state and turn phase. */
  case State(fightState: FightState, turn: TurnState)

  /** The player draws a card from the squirrel deck. */
  case DrawFromSquirrel

  /** The player draws a card from the main deck. */
  case DrawFromDeck

  /** The player plays a card at the given board position. */
  case CardToPlay(card: Card[?], position: Int)

  /** The player plays a card at the given board position, sacrificing the cards at the given positions. */
  case CardToPlayWithSacrifices(card: Card[?], position: Int, sacrificesPositions: List[BoardPosition])

  /** The player uses an item, optionally targeting a board position. */
  case UseItem(item: GameItem, target: Option[BoardPosition] = None)

  /** Signals that the player has ended their turn. */
  case EndPlayerTurn

  /** Signals the end of the fight. */
  case End

/** Messages for navigating the map. */
enum MapMessages extends GameMessage:
  case left
  case right
  case forward

/** Thread-safe synchronization channel between the Model and the View.
  *
  * Programming against this trait, rather than the concrete class, lets the rest of the system depend only on this
  * abstraction: the implementation can be swapped (e.g. with a stub for testing) without impacting the code that uses
  * it.
  */
trait GameMessagesChannel:
  /** Sends a message from the Model to the View. */
  def sendToGui(message: GameMessage): Unit

  /** Blocks until a message sent by the View is available, then returns it. */
  def receiveFromGui: GameMessage

  /** Blocks until a message sent by the Model is available, then returns it. */
  def receiveFromGame: GameMessage

  /** Sends a message from the View to the Model. */
  def sendToGame(message: GameMessage): Unit

  /** Empties both message queues. */
  def clear(): Unit

/** Concrete implementation of [[GameMessagesChannel]] built on two [[LinkedBlockingQueue]]s, one per direction (`toGui`
  * and `toGame`), turning the channel into a proper producer-consumer monitor.
  *
  * Read operations (`take()`) are not wrapped in the same `synchronized` block as writes: `LinkedBlockingQueue` is
  * already internally thread-safe for insertion and extraction, so the explicit lock only needs to coordinate writes
  * (and clearing) with each other, not reads, which can safely happen directly on the queue.
  */
class GameMessagesChannelImpl(
    private val toGui: LinkedBlockingQueue[GameMessage],
    private val toGame: LinkedBlockingQueue[GameMessage]
) extends GameMessagesChannel:

  /** Dedicated lock object shared by the two send operations and by `clear`. */
  private val lock = new Object

  override def sendToGui(message: GameMessage): Unit = lock.synchronized {
    toGui.put(message)
  }

  override def sendToGame(message: GameMessage): Unit = lock.synchronized {
    toGame.put(message)
  }

  override def receiveFromGui: GameMessage = toGame.take()
  override def receiveFromGame: GameMessage = toGui.take()

  override def clear(): Unit = lock.synchronized {
    toGui.clear()
    toGame.clear()
  }

/** Factory for [[GameMessagesChannel]]. */
object GameMessagesChannel:
  /** Creates a new channel backed by two fresh, empty blocking queues. */
  def apply(): GameMessagesChannel =
    new GameMessagesChannelImpl(
      new LinkedBlockingQueue[GameMessage](),
      new LinkedBlockingQueue[GameMessage]()
    )
