package na.weatherballoon.analysis

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import na.weatherballoon.analysis.aggregator.ResultAggregator
import na.weatherballoon.analysis.reader.{FeedConsumer, StartConsumeFeed}

object AnalysisApp extends App {

    val system = ActorSystem("AnalysisEngine")

    val config = ConfigFactory.load()

    val applicationConfig: Config = config getConfig "weather_balloon" getConfig "consumer_app"

    val balloonDump = applicationConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "path"

    val publisher = system.actorOf(ResultAggregator.props(), name = "publisher-guardian")

    val consumer = system.actorOf(FeedConsumer.props(applicationConfig, publisher), name = "consumer-guardian")

    consumer ! StartConsumeFeed(balloonDump)

    //Thread.sleep(600000) //wait 1 minute to allow enough time for the internal batch processors to complete

   // publisher ! PrintOutput

    //Thread.sleep(100000) // wait another 20 seconds, to see if the output will change, meaning that the record processing operation wasn't yet complete

    //publisher ! PrintOutput

    //Thread.sleep(1200000) // wait another 10 seconds, and confirm that the output hasn't changed  20 minutess

    //publisher ! PrintOutput

    //Thread.sleep(1200000) // wait another 6 seconds, and confirm that the output hasn't changed  20 minutess

    //val command = StdIn.readLine

    //val operations = if (command == "calculate") StdIn.readf("*,*") else "nader"

    //parse(operations)

    //print(operations)

    //publisher ! PrintOutput

    //consumer ! PoisonPill

    //implicit def parse(command: String): List[String] = command.split(",")
}
