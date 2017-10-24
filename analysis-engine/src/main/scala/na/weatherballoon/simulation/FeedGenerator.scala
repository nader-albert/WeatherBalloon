package na.weatherballoon.simulation

import java.time.LocalDateTime

import scala.util.Random

object FeedGenerator {

    def nextRecord: String = {
        val location = LocationGenerator.generate
        TimeGenerator.generate + "|" +  location._1 + "," + location._2 + "|" + TemperatureGenerator.generate + "|" + ObservatoryGenerator.generate
    }
}

trait Generator[T]{
    def generate: T
}

object TemperatureGenerator extends Generator[Int] {
    def generate: Int = if(Random.nextBoolean) - Random.nextInt(500) else Random.nextInt(500)
}

object LocationGenerator extends Generator[(Double, Double)] {
    def generate: (Double, Double) = (Random.nextDouble + Random.nextInt(5), Random.nextDouble + Random.nextInt(1000))
}

object ObservatoryGenerator extends Generator[String] {
    import Observatories._
    import DistanceUnits._
    import TemperatureUnits._

    val observatoryCodes: Map[String, (String, String)] =
        Map.empty[String, (String, String)]
            .withDefault(_ =>       (KELVIN, KILOMETERS))
            .updated(AUSTRALIA,     (CELSIUS, KILOMETERS))
            .updated(UNITED_STATES, (FAHRENHEIT, MILES))
            .updated(FRANCE,        (KELVIN, METER))
            .updated(BOLIVIA,       (KELVIN, KILOMETERS))
            .updated(BRAZIL,        (KELVIN, KILOMETERS))
            .updated(ECUADOR,       (KELVIN, KILOMETERS))

    def generate: String = {
        observatoryCodes.keys.drop(Random.nextInt(observatoryCodes.keys.size)).head
    }
}

object TimeGenerator extends Generator [LocalDateTime] {
    // Assuming that all simulation data will be on the same year and month, and spread on a range of two consecutive days
    override def generate: LocalDateTime = LocalDateTime.of(2017, 2, Random.nextInt(2) + 1, Random.nextInt(24), Random.nextInt(60))
}

object Observatories {
    val AUSTRALIA       = "AU"
    val UNITED_STATES   = "US"
    val FRANCE          = "FR"
    val ITALY           = "IT"
    val ECUADOR         = "EC"
    val BOLIVIA         = "BL"
    val BRAZIL          = "BR"
}

object TemperatureUnits {
    val KELVIN     = "kv"
    val CELSIUS    = "celsius"
    val FAHRENHEIT = "fahrenheit"
}

object DistanceUnits {
    val KILOMETERS  = "km"
    val MILES       = "miles"
    val METER       = "m"

}
