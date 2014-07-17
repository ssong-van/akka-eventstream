package com.typesafe.training.eventstream

import events._
import akka.actor.{Actor,ActorRef,Props}

object EventStreamSupervisor {
  def props : Props = Props(new EventStreamSupervisor)
  case class tick(request : List[Request])
}

class EventStreamSupervisor extends Actor {
  var users : Map[Long, ActorRef] = Map()

  def processTick(requests : List[Request]) : Unit = {
    val userActors = for {
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
    case EventStreamSupervisor.tick(request) => processTick(request)
  }

}
