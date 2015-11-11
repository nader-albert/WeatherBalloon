package com.cammy.weather.consume

import akka.actor._
import com.cammy.weather.process.{ProcessBatch, BatchProcessor, Ready}
import com.typesafe.config.Config

import scala.io.{BufferedSource, Source}
import scala.util.{Random, Success, Failure, Try}

/**
 * @author nader albert
 * Since   11/11/2015.
 */
case class StartConsumeFeed(fileName: String)
case object StopConsumeFeed

class FeedConsumer(consumerAppConfig: Config) extends Actor with ActorLogging {

  var feedLines: Option[Iterator[String]] = None

  var jobCounter = 0

  val chunkSize = Try {
    val chunk = consumerAppConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "batchSize"
    Integer.parseInt(chunk)
  } match {
    case Success(number) => number
    case Failure(ex) =>
      log error "couldn't parse default batch size configuration.. default is 1000 "
      1000
  }

  var processorPool: List[ActorRef] = Nil

  val workerPoolSize = Try {
    val workersSize = consumerAppConfig getConfig "sources" getConfig "files" getConfig "balloon-feed" getString "workerSize"
    Integer.parseInt(workersSize)
  } match {
    case Success(number) => number
    case Failure(ex) =>
      log error "couldn't parse default workers size configuration.. default is 100 "
      1000
  }

  override def receive: Receive = {
    case StartConsumeFeed(fileName) =>
      Try {
        Source fromURL getClass.getClassLoader.getResource(fileName)
      } match {
        case Failure(exception) => log error "file not loaded"
        case Success(src) =>
          log debug "feed file loaded successfully !"

          initialize(src)

          for (processor <- processorPool)
          yield nextBatch.fold(processor ! PoisonPill)(list => processor ! ProcessBatch(list))

          context become consuming(src, chunkSize)
      }
  }

  def consuming(source: BufferedSource, chunk: Int): Receive = {
    case Ready => println
      nextBatch.fold(sender ! PoisonPill)(sender ! ProcessBatch(_)) //kill the workers in turn, in case no more work to do
    case StopConsumeFeed =>
      log error "Feed Consumer is not connected to any source ! "
      feedLines = None
      context unbecome
  }

  private def initialize(src: BufferedSource) {
    feedLines = Some(src getLines)

    for (workerIndex <- 1 to workerPoolSize) {
      processorPool = processorPool.::(context.actorOf(Props[BatchProcessor], "batch-processor" + Random.nextInt))
    }

    //processorPool.head ! ProcessBatch(Nil)
  }

  private def nextBatch: Option[List[String]] = {
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
