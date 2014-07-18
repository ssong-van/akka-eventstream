package com.typesafe.training.eventstream

import events._
import akka.actor.{Actor,ActorRef,Props}
import scala.concurrent.duration._

object EventStreamSupervisor {
  def props : Props = Props(new EventStreamSupervisor)
  case class Tick(request : List[Request])
  case object InactivityPoll
  case class RemoveInactiveSession(sessionId: Long)
}

class EventStreamSupervisor extends Actor {
  var users : Map[Long, ActorRef] = Map()
  inactivityPoll

  val statsActor = context.actorOf(Stats.props, "stats")

  def inactivityPoll = {
    context.system.scheduler.scheduleOnce(5 minutes, self, EventStreamSupervisor.InactivityPoll)
  }

  def processTick(requests : List[Request]) : Unit = {
    val userActors: List[(ActorRef, Request)] = for {
      request <- requests
      userActor : ActorRef = users.getOrElse(request.session.id, {
        val userActor : ActorRef = context.actorOf(User.props(request.session.id))
        val entry = request.session.id -> userActor
        users += entry
        userActor
      })
    } yield (userActor, request)

    userActors map(userActorRequest => userActorRequest._1 ! User.AddEventToHistory(userActorRequest._2))
  }

  def receive = {
    case EventStreamSupervisor.Tick(request) => processTick(request)
    case EventStreamSupervisor.InactivityPoll => {
      users map( user => user._2 forward User.IsInactive(statsActor))
      inactivityPoll
    }
    case EventStreamSupervisor.RemoveInactiveSession(sessionId: Long) => {
      users -= sessionId
    }
  }

}
