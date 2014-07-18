package com.typesafe.training.eventstream

import akka.actor.{Props, Actor}

object Proxy {
  def props = Props(new Proxy)
}

class Proxy extends Actor {
  override def receive = ???
}
