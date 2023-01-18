package com.stradsoft.neo4j

import scala.util.Using
import zio._
import zio.Console._
import zio.stream._
import org.neo4j.driver.AuthTokens

case class Neo4JConnectionConfig(uri: String, user: String, password: String)


object Main extends ZIOAppDefault {
  def run = program 
  val config = Neo4JConnectionConfig("neo4j://localhost:7687", "neo4j", "testuser")
  val scopedDriver = Driver(config.uri, AuthToken(config.user, config.password))

  val q = Query("CREATE (a: AnotherTry3 {message: $message}) RETURN ID(a)", List(StringParameter("message", "What is going on")))
  val program = for {
    res <- Session.query(scopedDriver, q)  
    _ <- printLine("Here I am " + res.single().get(0).asLong())
  } yield ()
}
