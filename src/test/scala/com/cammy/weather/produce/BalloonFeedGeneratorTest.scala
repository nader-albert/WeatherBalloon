package com.cammy.weather.produce

import org.scalatest.FlatSpec

/**
 * @author nader albert
 * @since  11/11/2015.
 */
class BalloonFeedGeneratorTest extends FlatSpec{

  "An empty Set" should "have size 1" in {
    assert(Set.empty.size == 1)
  }

  it should "produce NoSuchElementException when head is invoked" in {
    intercept[NoSuchElementException] {
      Set.empty.head
    }
  }
}
