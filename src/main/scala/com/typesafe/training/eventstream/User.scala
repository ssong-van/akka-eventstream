package com.typesafe.training.eventstream

import events._
import akka.actor.{Actor,Props, ActorRef}

object User {
  def props(sessionId : Long) : Props = Props(new User(sessionId))

  case class AddEventToHistory(request : Request)
  case class IsInactive(statsActor: ActorRef)

  val fiveMinutesInMillis: Long = 5 * 60 * 1000
}

class User(val sessionId : Long) extends Actor {
  var events : List[Request] = List()

  var lastEventTimestamp: Long = System.currentTimeMillis()

  def inactive: Boolean = {
    (System.currentTimeMillis() - lastEventTimestamp) >= User.fiveMinutesInMillis
  }

  def receive : Receive = {
    case User.AddEventToHistory(request) => {
      events = events :+ request
      lastEventTimestamp = request.timestamp
    }

    case User.IsInactive(statsActor) => {
      if (inactive) statsActor ! Stats.UserInactive(sessionId, events)
    }
  }
}
