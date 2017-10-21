package na.weatherballoon.analysis.processor

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import na.weatherballoon.analysis.aggregator.PublishResult

import scala.util.{Failure, Success, Try}

case object Ready
case class Batch(records: List[String])

/**
 * responsible for processing a single batch
 * */
class BatchProcessor(resultPublisher: ActorRef) extends Actor with ActorLogging {
  //the actor state.

  //monitors the minTemp across all batches
  var minTemp: Int = -1
  //monitors the maxTemp across all batches
  var maxTemp: Int = -1
  //monitors the meanTemp across all batches
  var meanTemp: Int = -1

  val temperatureOrder: Ordering[Int] = Ordering.Int

  override def receive: Receive = {
    case batch: Batch =>
        log info "batch with : " + batch.records.size + " received"

        log info "initiating batch processing "

        process(batch.records)

        log info "batch processing ended "

        resultPublisher ! PublishResult(batch.records, maxTemp, minTemp, meanTemp)

        sender ! Ready
  }

  private def process(records: List[String]) {
      var timestamps :List[String]   = Nil
      var locations :List[Int]       = Nil
      var observatories :List[Int]   = Nil
      var logLineItems :List[String] = Nil
      var temperatureList :List[Int] = Nil

      //TODO: order batch records by timestamp

      val sequentialRecords = records.sortWith( (first, second) => LocalDateTime.parse(first.split("\\|").head).isAfter(LocalDateTime.parse(second.split("\\|").head)))

      sequentialRecords.foreach { record =>
          val sections = record.split("\\|").toList

          timestamps = timestamps.::(sections.head)

          val temperatureOption = sections.splitAt(2)._2.headOption

          if (temperatureOption.isDefined) {
              Try {
                  Integer.parseInt(temperatureOption.get)
              } match {
                  case Success(number) => temperatureList = temperatureList.::(number)
                  case Failure(ex) => log error "data corrupted | Temperature value couldn't be extracted "
              }
          }
    }

    val (minimum, maximum) = processTemperature(temperatureList.sortWith(_<_))

    if ( (temperatureOrder.gt(minTemp, minimum) && minimum > 0) || minTemp == -1)
        minTemp = minimum

    if ( (temperatureOrder.lt(maxTemp, maximum) && maxTemp > 0 && maximum > 0) || maxTemp == -1)
        maxTemp = maximum
  }

  private def processTemperature(orderedTemperatures: List[Int]): (Int, Int) =
      (orderedTemperatures.headOption.getOrElse(-1), orderedTemperatures.lastOption.getOrElse(-1))
}

object BatchProcessor{
    def props(resultPublisher: ActorRef) = Props(classOf[BatchProcessor], resultPublisher)
}
