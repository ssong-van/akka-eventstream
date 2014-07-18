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
  }

  def receive = {
    case EventStreamSupervisor.Tick(request) => processTick(request)
    //case EventStreamSupervisor.UserInactive(sessionId) => statsActor !
    case Stats.AverageVisitTime => statsActor forward Stats.AverageVisitTime
    case Stats.TopLandingPages(n) => statsActor forward Stats.TopLandingPages(n)
  }
}
