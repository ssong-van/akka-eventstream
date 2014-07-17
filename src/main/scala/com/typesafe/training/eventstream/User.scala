package com.typesafe.training.eventstream
import events._
import akka.actor.{Actor,Props, ActorRef}

object User {
  def props(sessionId : Long) : Props = Props(new User(sessionId))

  case class AddEventToHistory(request : Request)
}

class User(val sessionId : Long) extends Actor {
  var events : List[Request] = List()

  def receive : Receive = {
    case User.AddEventToHistory(request) => {
      events = events :+ request
    }
  }
}
