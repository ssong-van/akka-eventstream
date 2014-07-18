package com.typesafe.training.eventstream

import events._
import akka.actor.{Cancellable, Actor, Props}
import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer

object User {
  def props(sessionId : Long) : Props = Props(new User(sessionId))

  case class AddEventToHistory(requests : List[Request])
  case object Inactive
}

class User(val sessionId : Long) extends Actor {
  import context._

  val events: ListBuffer[Request] = ListBuffer()
  var scheduleToCancel : Option[Cancellable] = None

  def receive : Receive = {
    case User.AddEventToHistory(requests) => {
      scheduleToCancel match {
        case Some(schedulerToCancel) => schedulerToCancel.cancel()
        case None => //Do nothing
      }
      // Prepend to the existing requests
      events ++ requests
      scheduleToCancel = Some(context.system.scheduler.scheduleOnce(5 minutes, self, User.Inactive))
    }

    case User.Inactive => {
      context.parent ! Proxy.UserInactive(self)
    }
  }
}
