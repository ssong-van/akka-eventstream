organization := "com.typesafe.training"

name := "akka-eventstream"

libraryDependencies ++= Dependencies.eventStream

initialCommands := """|import akka.actor._
                      |import akka.actor.ActorDSL._
                      |import akka.pattern._
                      |import akka.routing._
                      |import akka.util._
                      |import com.typesafe.config._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._
                      |import com.typesafe.training.eventstream_""".stripMargin

