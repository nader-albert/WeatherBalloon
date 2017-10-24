package na.weatherballoon.query

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import na.weatherballoon.PrintOutput

import scala.io.StdIn

object QueryApp extends App {

    val system = ActorSystem("QueryEngine")

    val config = ConfigFactory.load()

    //val applicationConfig: Config = config getConfig "weather_balloon" getConfig "query_app"


    val command = StdIn.readLine

    /*val operations =
        if (command == "calculate") {
            StdIn.readLine()
        } else "No operation"


    print(operations)*/

    val xx = StdIn.readLine("calculate", new String)

    //parse(operations)

    print(xx)

    val selection = system.actorSelection("akka.tcp://AnalysisEngine@127.0.0.1:2552/user/publisher-guardian")

    selection ! PrintOutput

    print(xx)
    //implicit def parse(command: String): List[String] = command.split(",")

}