package na.weatherballoon.analysis.aggregator

import java.io.{FileNotFoundException, PrintWriter}

import akka.actor.{Actor, ActorLogging, Props}
import na.weatherballoon.{PrintOutput, Statistics}
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

            if (! statistics.exists(statistic =>
                statistic == Statistics.MaxTemp
                || statistic == Statistics.MeanTemp
                || statistic == Statistics.MinTemp
                || statistic == Statistics.TOTAL_DISTANCE
                || statistic == Statistics.OBSERVATIONS_PER_OBSERVATORY
                || statistic == Statistics.TOTAL_NUMBER_OF_OBSERVATIONS)) {
                log warning "invalid command"
            } else {
                fileWriter.append("**********************************************************************************" + "\r")

                statistics.foreach {
                    case Statistics.MaxTemp => recordMaxTemp()
                    case Statistics.MinTemp => recordMinTemp()
                    case Statistics.MeanTemp => recordMeanTemp()
                    case Statistics.TOTAL_DISTANCE => recordTotalDistance()
                    case Statistics.OBSERVATIONS_PER_OBSERVATORY => recordNumberOfObservations()
                    case Statistics.TOTAL_NUMBER_OF_OBSERVATIONS => recordTotalNumberOfRecords()
                    case _ =>
                }

                fileWriter.append("**********************************************************************************" + "\r")

                fileWriter.flush()
            }
    }

    private def calculateMeanTemperature = (maxTemp + minTemp) / totalNumberOfRecords

    private def recordMinTemp(): Unit = {
        fileWriter.append(" - Minimum Temperature: "     + "[" + minTemp      +  " " + TemperatureUnits.KELVIN            + " ]" + "\r")

        fileWriter.flush()
    }

    private def recordMaxTemp(): Unit = {
        fileWriter.append(" - Maximum Temperature: "     + "[" + maxTemp      +  " " + TemperatureUnits.KELVIN            + " ]" + "\r")

        fileWriter.flush()
    }

    private def recordMeanTemp(): Unit = {
        fileWriter.append(" - Mean Temperature: "        + "[" + meanTemp     +  " " + TemperatureUnits.KELVIN            + " ]" + "\r")

        fileWriter.flush()
    }

    private def recordTotalDistance(): Unit = {
        fileWriter.append(" - Total Distance Travelled " + "[" + distanceTravelled +  " " + DistanceUnits.KILOMETERS  + " ]" + "\r")

        fileWriter.flush()
    }

    private def recordNumberOfObservations(): Unit = {
        fileWriter.append(" - Number of Observations per Observatory " + "[" + observationsPerObservatory      + " ]" + "\r")

        fileWriter.flush()
    }

    private def recordTotalNumberOfRecords(): Unit = {
        fileWriter.append(
            " - Total Number of Record Processed "       + "[" + totalNumberOfRecords + "]" + "\r")
        fileWriter.flush()
    }

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

