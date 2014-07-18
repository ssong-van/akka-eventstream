package com.typesafe.training.eventstream

import akka.actor.{Props, Actor}
import events._

import org.joda.time.DateTime

import scala.collection.immutable
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.Set

object Stats {
  def props: Props = Props(new Stats)
  case class UserInactive(sessionId: Long, event: ListBuffer[Request])
  case class RecordRequest(request : Request)
  case object AverageVisitTime
  case class TopLandingPages(n: Int)
}

class Stats extends Actor{
  type Total = Int

  val inactiveSessions : Set[Long] = Set()
  val requests : ListBuffer[Request] = ListBuffer()
  val browserCount : Map[String, Total] = Map()
  val requestCountByMinute : Map[Int, Total] = Map()
  val pageCount : Map[String, Total] = Map()

  def recordStats(request : Request) = {
    // Number of requests per browser
    numRequestsPerBrowser(request)

    // Busiest minute of the day
    numRequestsPerMinute(request)

    // Number of visits per page
    pageCount += (request.url -> (pageCount.getOrElse(request.url, 0) + 1))
  }

  def averageVisitTime: Long = {
    val sortedByTime = requests.sortBy(r => r.timestamp).groupBy(r => r.session)
    val goodTimes = for {
      session <- sortedByTime
      totalDuration = Math.abs(session._2.head.timestamp - session._2.last.timestamp)
    } yield totalDuration
    goodTimes.sum / goodTimes.size
  }

  def topLandingPages(n: Int) = {
    pageCount.toSeq.sortWith(_._2 > _._2).take(n)
  }

  private def numRequestsPerBrowser(request: Request) = {
    val bc: Int = browserCount.getOrElse(request.session.browser,0)
    browserCount += (request.session.browser -> (bc+1))
  }

  private def numRequestsPerMinute(request: Request) {
    val minuteOfDay = new DateTime(request.timestamp).minuteOfDay().get()
    val requestCount = requestCountByMinute.getOrElse(minuteOfDay, 0)
    requestCountByMinute += (minuteOfDay -> (requestCount + 1))
  }

  override def receive = {
    case Stats.RecordRequest(request) => {
      request +=: requests
      recordStats(request)
    }

    case Stats.UserInactive(sessionId: Long, event: ListBuffer[Request]) => {
//      inactiveSessions += sessionId
//      sender() ! EventStreamSupervisor.RemoveInactiveSession(sessionId)
    }

    case Stats.AverageVisitTime => println(averageVisitTime)
    case Stats.TopLandingPages(n) => println(topLandingPages(n))
  }
}
