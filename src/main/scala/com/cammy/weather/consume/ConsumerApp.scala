package com.cammy.weather.consume

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author nader albert
 * @since   11/11/2015.
 */
object ConsumerApp extends App {

  val system = ActorSystem("WeatherBalloonSystem")

  val config = ConfigFactory load

  val applicationConfig: Config = config getConfig "weather_balloon" getConfig "consumer_app"

  val balloonDump = applicationConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "path"

  val consumer = system.actorOf(FeedConsumer.props(applicationConfig), name = "consumer-guardian")

  consumer ! StartConsumeFeed(balloonDump)

  Thread.sleep(550000)

  consumer ! StopConsumeFeed
}
