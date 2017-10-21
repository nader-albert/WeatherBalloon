package na.weatherballoon.simulation

import java.time.LocalDateTime

import scala.util.Random

object FeedSimulator {

    val timeGenerator        = new TimeGenerator
    val temperatureGenerator = new TemperatureGenerator
    val observatoryGenerator = new ObservatoryGenerator

    def nextRecord: String = {
        timeGenerator.generate + "|10,5|" + temperatureGenerator.generate + "|" + observatoryGenerator.generate
    }
}

trait Generator[T]{
    def generate: T
}

class TemperatureGenerator extends Generator[Int] {
    def generate: Int = Random.nextInt(500)
}

class ObservatoryGenerator extends Generator[String] {
    import Observatories._
    import DistanceUnits._
    import TemperatureUnits._

    val observatoryCodes: Map[String, (String, String)] =
        Map.empty[String, (String, String)]
            .withDefault(_ => (Kelvin, KM))
            .updated(AU, (Celsius, KM))
            .updated(US, (Fahrenheit, Miles))
            .updated(FR, (Kelvin, Meter))

    def generate: String = {
        observatoryCodes.keys.drop(Random.nextInt(observatoryCodes.keys.size)).head
    }
}

class TimeGenerator extends Generator [LocalDateTime] {
    // Assuming that all simulation data will be on the same year and month, and spread on a range of two consecutive days
    override def generate: LocalDateTime = LocalDateTime.of(2017, 2, Random.nextInt(2) + 1, Random.nextInt(24), Random.nextInt(60))
}

object Observatories {
    val AU = "AU"
    val US = "US"
    val FR = "FR"
    val IT = "IT"
}

object TemperatureUnits {
    val Kelvin     = "kelvin"
    val Celsius    = "celsius"
    val Fahrenheit = "fahrenheit"
}

object DistanceUnits {
    val KM     = "km"
    val Miles  = "miles"
    val Meter  = "m"

}
