package com.typesafe.training.eventstream

import akka.actor.{Actor, ActorRef, Props, PoisonPill}

import events._

object Proxy {
  def props = Props(new Proxy)

  case class ProxyRequest(requests: List[Request])
  case class UserInactive(user : ActorRef)
  case class UserSuspended(user : ActorRef)
}

class Proxy extends Actor {
  var users : scala.collection.mutable.Map[Long, ActorRef] = scala.collection.mutable.Map()

  def throttleUser(request : Request) : Boolean = false

  def killUserActor(user : ActorRef) : Unit = user ! PoisonPill

  def handleRequest(requests : List[Request]) = {
    val sessionToRequests : Map[Long, List[Request]] =
      requests.groupBy(_.session.id)

    sessionToRequests map {case(sessionid,requests) => {
      val userActor : ActorRef =
        users.getOrElseUpdate(sessionid, context.actorOf(User.props(sessionid)))

      if (requests.size > 100) self ! Proxy.UserSuspended(userActor)
      else userActor ! User.AddEventToHistory(requests)
    }}
  }

  def removeUser(user: ActorRef) : Unit = {
    user ! PoisonPill
    val sessionId : Option[Long] = (users map {_.swap}).get(user)
    sessionId match {
      case Some(id) => {
        sender() ! EventStreamSupervisor.UserInactive(id)
        users -= id
      }
      case None => //do nothing
    }
  }

  override def receive : Receive = {
    case Proxy.ProxyRequest(requests) => {
        handleRequest(requests)
    }

    case Proxy.UserSuspended(user) => {
      //Do stats stuff here
      removeUser(user)
    }

    case Proxy.UserInactive(user) => {
      removeUser(user)
    }
  }
}
