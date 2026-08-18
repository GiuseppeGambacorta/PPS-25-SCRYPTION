package org.scryption.game.model.items

import org.scryption.game.model.CardLibrary
import org.scryption.game.model.events.FightState

trait GameItem:
  def name: String
  def description: String

  /** Applies the items effect to the current fight state.
   *
   * @param state The current fight state.
   * @return the updated FightState.
   */
  def use(state: FightState): FightState


case class SquirrelBottle() extends GameItem:
  override def name: String = "Squirrel in a Bottle"
  override def description: String = "Break in case of emergency. Adds a Squirrel to your hand."

  override def use(state: FightState): FightState =
    val newHand = state.playerHand.addCard(CardLibrary.squirrel)
    val newInventory = state.inventory.filterNot(_ == this)
    state.copy(playerHand = newHand, inventory = newInventory)

case class HoggyBank() extends GameItem:
  override def name: String = "Hoggy Bank"
  override def description: String = "A small ceramic pig. Break it to gain 4 Bones."

  override def use(state: FightState): FightState =
    val newBones = state.bones + 4
    val newInventory = state.inventory.filterNot(_ == this)
    state.copy(bones = newBones, inventory = newInventory)

case class Pliers() extends GameItem:
  override def name: String = "Pliers"
  override def description: String = "Yank a tooth out. It is placed on your side of the scales."

  override def use(state: FightState): FightState =
    val newScale = state.scalePoints + 1
    val newInventory = state.inventory.filterNot(_ == this)
    state.copy(scalePoints = newScale, inventory = newInventory)
