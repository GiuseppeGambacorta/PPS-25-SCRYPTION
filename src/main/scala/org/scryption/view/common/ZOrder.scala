package org.scryption.view.common

import javax.swing.JLabel

/** Shared back-to-front stacking rule for hand-of-cards views: cards sit in their
 *  natural index order, the hovered-but-not-slotted card floats above those, and
 *  whichever card is in the slot is always frontmost of all. Every view with a hand
 *  and a slot (Strange Stones, both fire camp views, Mycologists) had this exact
 *  algorithm copy-pasted; it's now defined once here.
 *
 *  `labelsOf` returns every Swing label a single item owns (usually one, but
 *  Mycologists' duplicate-card slot owns two), so both cases can share this.
 */
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

    // setComponentZOrder: 0 = frontmost, so assign in reverse
    labelsBackToFront.reverse.zipWithIndex.foreach { case (label, z) =>
      parent.setComponentZOrder(label, z)
    }
  }
}
