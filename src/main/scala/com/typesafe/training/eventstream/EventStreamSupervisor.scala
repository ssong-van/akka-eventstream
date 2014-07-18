package com.typesafe.training.eventstream

import events._
import akka.actor.{Actor,Props}


object EventStreamSupervisor {
  def props: Props = Props(new EventStreamSupervisor)

  case class Tick(request: List[Request])
  case class UserInactive(sessionId: Long)

}

class EventStreamSupervisor extends Actor {
  val proxyActor = context.actorOf(Proxy.props, "proxy")
  val statsActor = context.actorOf(Stats.props, "stats")

  def processTick(requests : List[Request]) : Unit = {
    proxyActor ! Proxy.ProxyRequest(requests)

    // Record stats
    requests map (r => statsActor ! Stats.RecordRequest(r))
  }

  def receive = {
    case EventStreamSupervisor.Tick(request) => processTick(request)
    case EventStreamSupervisor.UserInactive(sessionId) => statsActor ! Stats.UserInactive(sessionId)
    case Stats.AverageVisitTime => statsActor forward Stats.AverageVisitTime
    case Stats.TopLandingPages(n) => statsActor forward Stats.TopLandingPages(n)
    case Stats.NumberOfRequestsPerBrowser => statsActor forward Stats.NumberOfRequestsPerBrowser
    case Stats.BusiestMinute => statsActor forward Stats.BusiestMinute
  }
}
