package na.weatherballoon.analysis

import akka.actor.{ActorSystem, PoisonPill}
import com.typesafe.config.{Config, ConfigFactory}
import na.weatherballoon.analysis.reader.{FeedConsumer, StartConsumeFeed}

object App extends App {

  val system = ActorSystem("WeatherBalloonSystem")

  val config = ConfigFactory.load()

  val applicationConfig: Config = config getConfig "weather_balloon" getConfig "consumer_app"

  val balloonDump = applicationConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "path"

  val consumer = system.actorOf(FeedConsumer.props(applicationConfig), name = "consumer-guardian")

  consumer ! StartConsumeFeed(balloonDump)

  Thread.sleep(5500)

  consumer ! PoisonPill
}
