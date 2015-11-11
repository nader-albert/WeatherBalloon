package com.cammy.weather.produce

import scala.util.Random


/**
 * @author nader albert
 * @since  11/11/2015.
 */
object FeedSimulator {

  val temperatureGenerator = new TemperatureGenerator
  val observatoryGenerator = new ObservatoryGenerator


  def nextLog: String = {

    "2014-12-31T13:44|10,5|" + temperatureGenerator.generate + "|" + observatoryGenerator.generate
  }
}

trait Generator[Int]{
  def generate: Int
}

class TemperatureGenerator[Int] {
  def generate = Random.nextInt(500) //TODO: define these numbers as constants and give them names
}

class ObservatoryGenerator[String] {
  val observatoryCodes = List("AU", "US", "FR", "IT", "NZ", "SP")

  def generate = {
    observatoryCodes(Random.nextInt(5))
  }
}
