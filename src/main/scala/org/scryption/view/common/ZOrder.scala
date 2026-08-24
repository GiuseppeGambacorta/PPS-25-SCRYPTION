package org.scryption.view.common

import javax.swing.JLabel

object ZOrder {

  def apply[A](
      items: Vector[A],
      isInSlot: A => Boolean,
      isHovered: A => Boolean,
      index: A => Int,
      labelsOf: A => List[JLabel]
  ): Unit = {
    if (items.isEmpty) return
    val parent = labelsOf(items.head).headOption.flatMap(l => Option(l.getParent)).orNull
    if (parent == null) return

    val inSlot = items.find(isInSlot)
    val hovered = items.find(a => isHovered(a) && !isInSlot(a))
    val base = items.filterNot(a => isInSlot(a) || hovered.contains(a)).sortBy(index)
    val backToFront = base ++ hovered.toList ++ inSlot.toList
    val labelsBackToFront = backToFront.flatMap(labelsOf)

    labelsBackToFront.reverse.zipWithIndex.foreach { case (label, z) =>
      parent.setComponentZOrder(label, z)
    }
  }
}
