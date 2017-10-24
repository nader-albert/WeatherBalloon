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

}
