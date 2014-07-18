package events

import com.typesafe.training.eventstream._
import akka.actor._

object EventStreamSample extends App {
  val system = ActorSystem("supervisor")
  val supervisor = system.actorOf(EventStreamSupervisor.props)
  val stream = new EventStream(5)

  for {
    i <- 1 to 20
    requests = stream.tick
  } supervisor ! (EventStreamSupervisor.Tick(requests))

  supervisor ! Stats.AverageVisitTime
  supervisor ! Stats.TopLandingPages(5)
  supervisor ! Stats.NumberOfRequestsPerBrowser
  supervisor ! Stats.BusiestMinute
}
