package com.cammy.weather.process

import akka.actor.{Actor, ActorLogging}

/**
 * @author nader albert
 * @since  11/11/2015.
 */
case object Ready
case class ProcessBatch(logs: List[String])

class BatchProcessor extends Actor with ActorLogging {

  override def receive: Receive = {
    case processBatch: ProcessBatch =>
      println("process batch received")
      process(processBatch.logs)

      sender ! Ready
    case _ => println ("any message")
  }

  private def process(logs: List[String]): Unit ={
    //println("start processing batch .......")
    logs.foreach(log => "*****" + println(log))
    //println("end processing batch .......")
  }
}
