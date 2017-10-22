package na.weatherballoon.analysis.processor

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import na.weatherballoon.simulation.{Observatories, TemperatureUnits, DistanceUnits}
import na.weatherballoon.{Distance, Location, Observatory, Temperature}
import scala.math._

case object Ready
case class Batch(records: List[String])

/**
 * responsible for processing a single batch
 * */
class BatchProcessor(resultPublisher: ActorRef) extends Actor with ActorLogging {

    override def receive: Receive = {
        case batch: Batch =>
            log info "batch with : " + batch.records.size + " received"

            log info "initiating batch processing "

            val output = process(batch.records)

            log info "batch processing ended "

            resultPublisher ! output

            sender ! Ready
    }

    private def process(records: List[String]): BatchResult = {
        val standardRecords = records.map(parse)
        val orderedRecords = standardRecords.sortWith((first, second) => first.time.isBefore(second.time))

        val classification = classify(standardRecords.map(record => record.observatory)).mapValues(_.length)

        val (high, low) = calculateTemperatureBoundaries(standardRecords.map(record => (record.temperature, record.observatory)))

        val distanceTravelled = calculateTravelledDistance(orderedRecords.map(record => (record.coordinates, record.observatory)))

        println(distanceTravelled)

        BatchResult(low, high, distanceTravelled, classification)
    }

    /***
      * transforms a record in a text format to its standard domain model equivalent
      * */
    private def parse(record: String): Record = {
        val partitions = record.split("\\|").toSeq

        val coordinates = partitions.tail.head.split(",").toSeq

        Record (
            LocalDateTime.parse(partitions.head),
            Location(coordinates.head.toFloat, coordinates.last.toFloat),
            Temperature(partitions.drop(2).head.toFloat, ""),
            Observatory(partitions.drop(3).head)
        )
    }

    private def classify(observatories :Seq[Observatory]) = observatories.groupBy(_.code)

    /**
      * calculates the minimum and the maximum temperature, in the current batch of records
      * @param temperatures, a sequence of temperature records belonging to the current batch, with the corresponding
      *                      observatory associated with each record
      * @return a tuple, representing the minimum and the maximum temperatures in the given list, where the first item
      *         represents the high temperature and the second represents the low temperature
      * */
    private def calculateTemperatureBoundaries(temperatures: Seq[(Temperature, Observatory)]): (Temperature, Temperature) = {
        import TemperatureUnits._

        val normalisedTemperatures = normalizeTemperatures(temperatures).sortWith(_.value > _.value)

        (normalisedTemperatures.headOption.getOrElse(Temperature(-1, Kelvin)), normalisedTemperatures.lastOption.getOrElse(Temperature(-1, Kelvin)))
    }

    /**
      * calculates the total travelled distance of the balloon, based on the current batch of records
      * @param locations, a sequence of location records belonging to the current batch, with the corresponding observatory for each of them
      * @return the distance travelled in Kilometers
      * */
    private def calculateTravelledDistance(locations: Seq[(Location, Observatory)]): Distance = {
        import DistanceUnits._

        def measure(locations: List[Location]): Double = {
            locations match {
                case Nil => 0.0
                case head::Nil => 0.0
                case head::tail =>
                    val currentCoordinate = tail.last
                    val previousCoordinate = tail.drop(tail.length - 2).head

                    sqrt(
                        pow(currentCoordinate.x - previousCoordinate.x, 2)
                            + pow(currentCoordinate.y - previousCoordinate.y, 2)
                    ) + measure(locations.take(locations.length -1))

                // if size < 2 return 0
                // return root ( sq(x1 - x0) + sq(y1 -y0) ) + measure (list drop length _ 1)
            }
        }

        Distance(measure(normalizeLocations(locations).toList), KILOMETERS)
    }

    /**
      * normalises the temperatures by unifying the unit of measurement to the domain unit (Kelvin)
      * */
    private def normalizeTemperatures(temperatureRecords: Seq[(Temperature, Observatory)]) = {
        import Observatories._
        import TemperatureUnits._

        def celsiusToKelvin(celsius: Temperature) = celsius.value + 273.15

        def fahrenheitToKelvin(fahrenheit: Temperature) = (fahrenheit.value + 459.67) * 5/9

        temperatureRecords
            .map(record => {
                if (record._2.code == AUSTRALIA) {
                    Temperature(celsiusToKelvin(record._1), Kelvin)
                } else if (record._2.code == UNITED_STATES) {
                    Temperature(fahrenheitToKelvin(record._1), Kelvin)
                } else {
                    record._1.copy(unit = Kelvin)
                }
            })
    }

    /**
      * normalises the coordinates by unifying the unit of measurement to the domain unit (Kilometer)
      */
    private def normalizeLocations(locationRecords: Seq[(Location, Observatory)]) = {
        import Observatories._

        def mileToKilometer(coordinates: Location) = Location(coordinates.x * 1.609344, coordinates.y * 1.609344)

        def meterToKilometer(coordinates: Location) = Location(coordinates.x /1000, coordinates.y / 1000)

        locationRecords
            .map(record => {
                    if (record._2.code == FRANCE) {
                        meterToKilometer(record._1)
                    } else if (record._2.code == UNITED_STATES) {
                        mileToKilometer(record._1)
                    } else {
                        record._1
                    }
                })
    }
}

case class Record(time: LocalDateTime, coordinates: Location, temperature: Temperature, observatory: Observatory)

case class BatchResult(minTemperature: Temperature, maxTemperature: Temperature, totalDistance: Distance, observationClassification :Map[String, Int])

object BatchProcessor{
    def props(resultPublisher: ActorRef) = Props(classOf[BatchProcessor], resultPublisher)
}
