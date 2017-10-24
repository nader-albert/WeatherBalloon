package na.weatherballoon.analysis.aggregator

import java.io.{FileNotFoundException, PrintWriter}

import akka.actor.{Actor, ActorLogging, Props}
import na.weatherballoon.PrintOutput
import na.weatherballoon.analysis.processor.BatchResult
import na.weatherballoon.simulation.{DistanceUnits, Observatories, TemperatureUnits}

import scala.util.{Failure, Success, Try}

class ResultAggregator extends Actor with ActorLogging {

    val outputFile = "balloon_weather_output" // TODO: should come from configuration file

    import Observatories._
    var minTemp: Double = +10000
    var maxTemp: Double = -1
    var meanTemp: Double = -1
    var distanceTravelled: Double = 0.0
    var observationsPerObservatory: Map[String, Int] = Map.empty[String, Int].updated(UNITED_STATES, 0).updated(AUSTRALIA, 0)
    var totalNumberOfRecords: Long = 0

    var fileWriter: PrintWriter = null

    override def preStart(): Unit = {
        Try {
          new PrintWriter(outputFile)
        } match {
            case Failure(ex: FileNotFoundException) => println("file not found")
            case Success(src: PrintWriter) => fileWriter = src
                print("file loaded successfully !")
        }
    }

    override def postStop(): Unit = {
        fileWriter close()
    }

    override def receive: Receive = {
        case result: BatchResult =>
            if (result.minTemperature.value < minTemp) minTemp = result.minTemperature.value

            if (result.maxTemperature.value > maxTemp) maxTemp = result.maxTemperature.value

            observationsPerObservatory = classifyObservatories(result.observationClassification)

            totalNumberOfRecords = totalNumberOfRecords + result.batchSize

            distanceTravelled = distanceTravelled + result.totalDistance.value

            meanTemp = calculateMeanTemperature

        case PrintOutput(statistics) =>
            log info "printing results"
            recordResults()
    }

    private def calculateMeanTemperature = (maxTemp + minTemp) / totalNumberOfRecords

    private def recordResults(): Unit = {
        fileWriter.append(

            "**********************************************************************************" + "\r" +

            " - Maximum Temperature: "     + "[" + maxTemp      +  " " + TemperatureUnits.KELVIN            + " ]" + "\r" +
            " - Minimum Temperature: "     + "[" + minTemp      +  " " + TemperatureUnits.KELVIN            + " ]" + "\r" +
            " - Mean Temperature: "        + "[" + meanTemp     +  " " + TemperatureUnits.KELVIN            + " ]" + "\r" +
            " - Total Distance Travelled " + "[" + distanceTravelled +  " " + DistanceUnits.KILOMETERS  + " ]" + "\r" +
            " - Number of Observations per Observatory " + "[" + observationsPerObservatory      + " ]" + "\r" +
            " - Total Number of Record Processed "       + "[" + totalNumberOfRecords + "]" + "\r" +

            "**********************************************************************************"
        )

        fileWriter.flush()
    }

    private def classifyObservatories(observatoryClassification: Map[String, Int]) = {
        (observationsPerObservatory.keySet ++ observatoryClassification.keySet)
            .map { key => (key, observationsPerObservatory.getOrElse(key, 0) + observatoryClassification.getOrElse(key, 0))
        }.toMap

    }
}

object ResultAggregator {
    def props() = Props(classOf[ResultAggregator])
}

