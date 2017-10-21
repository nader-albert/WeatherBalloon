package na.weatherballoon.publish

import java.io.{FileNotFoundException, PrintWriter}

import akka.actor.{Actor, ActorLogging}

import scala.util.{Failure, Success, Try}

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


          fileWriter.append(
              "Maximum Temperature: " + "[" + publish.maxTemp + "]" + " " +
              "Minimum Temperature: " + "[" + publish.minTemp + "]" + " " +
              "Mean Temperature: "    + publish.meanTemp + "\r"
          )

            fileWriter.close()
    }
}
