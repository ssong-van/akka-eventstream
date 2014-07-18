package com.typesafe.training.eventstream

import akka.actor.{Props, Actor}
import events._

import scala.collection.mutable.ListBuffer

object Stats {
  def props: Props = Props(new Stats)
  case class UserInactive(sessionId: Long, event: ListBuffer[Request])
  case class RecordRequest(request : Request)
}

class Stats extends Actor{
  var inactiveSessions : Set[Long] = Set()
  var requests : List[Request] = List()
  var browserCount : Map[String, Int] = Map()

  def recordStats(request : Request) = {
    val count: Int = browserCount.getOrElse(request.session.browser,0)
    browserCount += (request.session.browser -> (count+1))
  }

  override def receive = {
    case Stats.RecordRequest(request) => {
      requests = requests :+ request
      recordStats(request)
    }

    case Stats.UserInactive(sessionId: Long, event: ListBuffer[Request]) => {
      inactiveSessions += sessionId
      sender() ! EventStreamSupervisor.RemoveInactiveSession(sessionId)
    }
  }

}
