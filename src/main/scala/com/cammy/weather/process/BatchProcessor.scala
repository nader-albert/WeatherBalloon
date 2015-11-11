package com.cammy.weather.process

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import com.cammy.weather.publish.PublishResult

import scala.util.{Try, Failure, Success}

/**
 * @author nader albert
 * @since  11/11/2015.
 */
case object Ready
case class ProcessBatch(logs: List[String])

/**
 * responsible for processing a single batch
 * */
class BatchProcessor(resultPublisher: ActorRef) extends Actor with ActorLogging {
  //the actor state.

  //monitors the minTemp across all batches
  var minTemp = 0
  //monitors the maxTemp across all batches
  var maxTemp = 0
  //monitors the meanTemp across all batches
  var meanTemp = 0

  val temperatureOrder: Ordering[Int] = Ordering.Int

  override def receive: Receive = {
    case processBatch: ProcessBatch =>
      process(processBatch.logs)

      resultPublisher ! PublishResult(processBatch.logs, maxTemp, minTemp, meanTemp)
      sender ! Ready
  }

  private def process(logLines: List[String]) {
    var logLineItems :List[String] = Nil

    var temperatureList: List[Int] = Nil
    var locationList: List[Int] = Nil
    var observatoryList: List[Int] = Nil
    var timeList: List[String] = Nil

    logLines.foreach { logLine =>
      val listOfLogItems = logLine.split("\\|").toList

      timeList = timeList.::(listOfLogItems.head)

      val temperatureOption = listOfLogItems.splitAt(2)._2.headOption

      if (temperatureOption.isDefined)
        Try {
          Integer.parseInt(temperatureOption.get)
        } match {
          case Success(number) => temperatureList = temperatureList.::(number)
          case Failure(ex) => log error "data corrupted | Temperature value couldn't be extracted "
        }
    }

    val minMaxTemperatures = processTemperature(temperatureList.sortWith(_<_))

    if (temperatureOrder.gt(minTemp,minMaxTemperatures._1) && minTemp > 0 && minMaxTemperatures._1 > 0)
      minTemp = minMaxTemperatures._1

    if (temperatureOrder.lt(maxTemp,minMaxTemperatures._2) && maxTemp > 0 && minMaxTemperatures._2 > 0)
      maxTemp = minMaxTemperatures._2
  }

  private def processTemperature(orderedTemperatures: List[Int]): (Int, Int) =
    (orderedTemperatures.headOption.getOrElse(-1), orderedTemperatures.lastOption.getOrElse(-1))
}

object BatchProcessor{
  def props(resultPublisher: ActorRef) = Props(classOf[BatchProcessor], resultPublisher)
}
