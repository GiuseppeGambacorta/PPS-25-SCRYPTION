package org.scryption

import org.scalatest.flatspec.AnyFlatSpec

class MultiSchermataSwingTest extends AnyFlatSpec:

  "Il contatore" should "incrementare il valore di uno" in {
    assert(Main.incrementCounter(0) == 1)
    assert(Main.incrementCounter(41) == 42)
  }
