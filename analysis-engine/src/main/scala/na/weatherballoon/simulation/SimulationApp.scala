package na.weatherballoon.simulation

import java.io.{FileNotFoundException, PrintWriter}

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}
import languageFeature.postfixOps

object SimulationApp extends App {

    val config = ConfigFactory.load()

    val applicationConfig: Config = config getConfig "weather_balloon" getConfig "producer_app"

    //TODO: don't take it for granted that the configurations already exist.
    val defaultOutputFile = applicationConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "path"

    val file = args.toSeq.find(_ startsWith "-f=" ).fold(defaultOutputFile)(file => file.substring(file.indexOf("=") + 1))

    Try {
        new PrintWriter(file)
    } match {
        case Failure(ex:FileNotFoundException) => println ("file not found")
        case Success(src) => print("file loaded successfully !")

        import na.weatherballoon.simulation.FeedGenerator._

        for (i <- 1 to 500000000) {
            src.write(nextRecord + "\r")
        }

        print ("finished loading the file ")
        src close()
    }
}
