package na.weatherballoon.analysis.reader

import akka.actor._
import com.typesafe.config.Config
import na.weatherballoon.analysis.processor.{Batch, BatchProcessor, Ready}
import na.weatherballoon.analysis.aggregator.ResultPublisher

import scala.io.{BufferedSource, Source}
import scala.languageFeature.postfixOps
import scala.util.{Failure, Random, Success, Try}

case class StartConsumeFeed(fileName: String)
case object StopConsumeFeed

class FeedConsumer(consumerAppConfig: Config) extends Actor with ActorLogging {

    var feedLines: Option[Iterator[String]] = None

    var jobCounter = 0

    val chunkSize: Int = Try {
            Integer.parseInt(consumerAppConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "batchSize")
        } match {
            case Success(number) => number
            case Failure(ex) =>
                log error ("couldn't parse default batch size configuration.. default is 1000 ", ex)
                1000
        }

    var processorPool: List[ActorRef] = Nil

    val workerPoolSize: Int = Try {
        Integer.parseInt(consumerAppConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "workerSize")
    } match {
        case Success(number) => number
        case Failure(ex) =>
            log error ("couldn't parse default workers size configuration.. default is 100 " , ex)
            1000
    }

    /**
      * */
    override def receive: Receive = {
        case StartConsumeFeed(fileName) =>
            Try {
                Source.fromURL(getClass.getClassLoader.getResource(fileName))
            } match {
                case Failure(exception) => log error "file not loaded" + exception

                case Success(src) => log debug "feed file loaded successfully !"

                initialize(src)

                for (processor <- processorPool)
                    yield readNext().fold(processor ! PoisonPill)(list => processor ! Batch(list))

                context become consuming(src, chunkSize)
            }
    }

    /**
      * */
    def consuming(source: BufferedSource, chunk: Int): Receive = {
        case Ready => readNext().fold(sender ! PoisonPill)(sender ! Batch(_)) //kill the workers in turn, in case no more work to do

        case StopConsumeFeed => log error "Feed Consumer is not connected to any source ! "
            feedLines = None
            context.unbecome
    }

    private def initialize(src: BufferedSource) {
        feedLines = Some(src.getLines)

        val resultPublisherActor = context.actorOf(Props[ResultPublisher], "result-publisher")

        for (index <- 1 to workerPoolSize) {
            processorPool = processorPool.::(context.actorOf(BatchProcessor.props(resultPublisherActor), "batch-processor" + Random.nextInt))
        }
    }

    private def readNext(): Option[List[String]] = {
        var batch: List[String] = Nil

        var numberOfLines = 0

        while (feedLines.fold(false)(_.hasNext) && numberOfLines < chunkSize) {
          numberOfLines = numberOfLines + 1

          batch = batch.::(feedLines.get.next)
          //it is safe to call get on an option directly, since a None,
          //wouldn't have reached this point, due to the enclosing loop condition
        }

        batch match {
          case Nil => None
          case head::list => Some(head::list)
        }
    }
}

object FeedConsumer{
    def props(consumerAppConfig: Config) = Props(classOf[FeedConsumer], consumerAppConfig)
}
