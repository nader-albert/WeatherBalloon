package na.weatherballoon.query

import akka.actor.ActorSystem
import na.weatherballoon.{PrintOutput, Statistics}

import scala.io.StdIn

object QueryApp extends App {

    val system = ActorSystem("QueryEngine")

    printWelcomeMessage()

    //TODO: Name of the Analysis Engine should be retrieved from application configuration
    val aggregator = system.actorSelection("akka.tcp://AnalysisEngine@127.0.0.1:2552/user/publisher-guardian")

    /**
      **/
    private def parse(command: String): Unit = {
        command match {
            case Statistics.MaxTemp =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.MaxTemp))
                parse(StdIn.readLine)

            case Statistics.MinTemp =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.MinTemp))
                parse(StdIn.readLine)

            case Statistics.MeanTemp =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.MeanTemp))
                parse(StdIn.readLine)

            case Statistics.TOTAL_DISTANCE =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.TOTAL_DISTANCE))
                parse(StdIn.readLine)

            case Statistics.OBSERVATIONS_PER_OBSERVATORY =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.OBSERVATIONS_PER_OBSERVATORY))
                parse(StdIn.readLine)

            case Statistics.TOTAL_NUMBER_OF_OBSERVATIONS =>
                aggregator ! PrintOutput(Seq.empty[String].+:(Statistics.TOTAL_NUMBER_OF_OBSERVATIONS))
                parse(StdIn.readLine)

            case statistics: String if statistics.matches("(?:" + Statistics.MaxTemp + " | " + Statistics.MinTemp + ")") =>
                aggregator ! PrintOutput(statistics.split("-").toSeq)
                parse(StdIn.readLine)

            case "Quit" =>
                println("exiting ......")
                System.exit(1)

            case _ =>
                print("not a valid command, please try again")
                parse(StdIn.readLine)
        }
    }

    private def printWelcomeMessage(): Unit = {
        println("\n ********************************************************************************** \n ")

        println("Enter one or more commands from the list below: - NB: Multiple commands must be separated by `-`. \n ")

        println("Example: MaxTemp - MinTemp - \n ")

        println(
            " [" + "        " + Statistics.MaxTemp + "         "+ "] :: calculates the maximum temperature that have been recorded so far "          + "\n" +
            " [" + "        " + Statistics.MinTemp + "         "+ "] :: calculates the minimum temperature that have been recorded so far "          + "\n" +
            " [" + "        " + Statistics.MeanTemp+ "        "+ "] :: calculates the mean temperature that have been recorded so far "             + "\n" +
            " [" + "        " + Statistics.TOTAL_DISTANCE+"       "+ "] :: calculates the total distance travelled so far"                              + "\n" +
            " [" + "       " + Statistics.OBSERVATIONS_PER_OBSERVATORY + "      " + "] :: calculates the number of observations recorded per each observatory so far " + "\n" +
            " [" + "       " + Statistics.TOTAL_NUMBER_OF_OBSERVATIONS + "     " + "] :: calculates the total number of observations recorded so far"                 + "\n" +
            " [" + "Quit" + " ] :: to exit query app")

        parse(StdIn.readLine)
    }
}