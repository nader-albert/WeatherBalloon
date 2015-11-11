package com.cammy.weather.publish

import java.io.{PrintWriter, FileNotFoundException}

import akka.actor.{ActorLogging, Actor}

import scala.util.{Success, Failure, Try}

/**
 * @author nader albert
 * @since  11/11/2015.
 */
case class PublishResult(logBatch: List[String], maxTemp: Int, minTemp: Int, meanTemp: Int)

class ResultPublisher extends Actor with ActorLogging {

  val outputFile = "balloon_weather_output" // TODO: should come from configuration file

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

  override def postStop(): Unit ={
    fileWriter close
  }

  override def receive: Receive = {
    //TODO: what is printed should be coming from command line
    case publish: PublishResult =>

      /*println (
      "[ *********** " +
        "Maximum Temperature recorded so far is:  " + publish.maxTemp + "Minimum Temperature recorded so far is:  " + publish.maxTemp + "\r" +
        //"Mean Temperature recorded so far is:  " + publish.maxTemp + "\r" +
        //"Number of observations for each Observatory " + //TODO; See how to get this
        //"Total Distance Travelled so far is: " +
      " ************ ]"
    )*/

      for (i <- 1 to 10000) {
        fileWriter.write(
            "Maximum Temperature: " + publish.maxTemp +
            "Minimum Temperature: " + publish.minTemp +
            "Mean Temperature: " + publish.meanTemp
        )
      }
    }
}
