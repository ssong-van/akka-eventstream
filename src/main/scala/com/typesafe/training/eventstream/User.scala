package com.typesafe.training.eventstream

import events._
import akka.actor.{Actor,Props, ActorRef}

import scala.collection.mutable.ListBuffer

object User {
  def props(sessionId : Long) : Props = Props(new User(sessionId))

  case class AddEventToHistory(request : Request)
  case class IsInactive(statsActor: ActorRef)

  val fiveMinutesInMillis: Long = 5 * 60 * 1000
}

class User(val sessionId : Long) extends Actor {
  val events: ListBuffer[Request] = ListBuffer()

  def inactive: Boolean = {
    // Head is the latest event
    (System.currentTimeMillis() - events.head.timestamp) >= User.fiveMinutesInMillis
  }

  def receive : Receive = {
    case User.AddEventToHistory(request) => {
      // Prepend to the existing requests
      request +=: events
    }

    case User.IsInactive(statsActor) => {
      if (inactive) statsActor ! Stats.UserInactive(sessionId, events)
    }
  }
}
