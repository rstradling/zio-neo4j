package com.stradsoft.neo4j

import org.neo4j.driver.{ Session => NeoSession, 
                          Driver => NeoDriver, 
                          AuthToken => NeoAuthToken, 
                          AuthTokens => NeoAuthTokens, 
                          Query => NeoQuery,
                          GraphDatabase }
import zio._


case class AuthToken(private[neo4j] val token: NeoAuthToken)

object AuthToken {
  def apply(username: String, password: String) : AuthToken = {
    apply(username, password, null)
  }
  def apply(username: String, password: String, realm: String) : AuthToken = {
    AuthToken(NeoAuthTokens.basic(username, password, realm))
  }
}

case class Driver(driver: NeoDriver) extends AutoCloseable {
  override def close(): Unit = {
    driver.close()
  }
}

object Driver {
  def apply(uri: String, authToken: AuthToken) : ZIO[Scope, Throwable, Driver] = {
    ZIO.fromAutoCloseable(ZIO.attempt(Driver(GraphDatabase.driver(uri, authToken.token))))
  }
}


sealed trait QueryParameter {
  def name: String
}


final case class IntParameter(name: String, v: Int) extends QueryParameter
final case class LongParameter(name: String, v: Long) extends QueryParameter
final case class BoolParameter(name: String, v: Boolean) extends QueryParameter
final case class DoubleParameter(name: String, v: Double) extends QueryParameter
final case class FloatParameter(name: String, v: Float) extends QueryParameter
final case class StringParameter(name: String, v: String) extends QueryParameter
final case class MapParameter[T <: QueryParameter](name: String, v: Map[String, T]) extends QueryParameter
final case class ListParameter[T <: QueryParameter](name: String, v: List[T]) extends QueryParameter

object QueryParameter
{
  def toNeoParameters(parameters: List[QueryParameter]): Map[String, Object] = {
    val list : List[(String, AnyRef)] = parameters.map{ p =>
      p match {
            case IntParameter(name, v) => (name, v.asInstanceOf[AnyRef]) 
            case LongParameter(name, v) => (name, v.asInstanceOf[AnyRef]) 
            case BoolParameter(name, v) => (name, v.asInstanceOf[AnyRef]) 
            case DoubleParameter(name, v) => (name, v.asInstanceOf[AnyRef])
            case FloatParameter(name, v) => (name, v.asInstanceOf[AnyRef])
            case StringParameter(name, v) => (name, v)
            case MapParameter(name, v) => (name, v)
            case ListParameter(name, v) => (name, v)
      }
    }
    Map.from(list)
  }
}

case class Session(session: NeoSession)

case class Query(queryString: String, parameters : List[QueryParameter]) 
object Query {
  def toNeoQuery(q: Query): NeoQuery = {
    new NeoQuery(q.queryString, scala.collection.JavaConverters.mapAsJavaMap(QueryParameter.toNeoParameters(q.parameters)))
  }
}



object Session {
  def acquire(dr: => Driver): ZIO[Any, Throwable, Session] = ZIO.attempt(Session(dr.driver.session()))
  def release(session: => Session): ZIO[Any, Nothing, Unit] = ZIO.succeed(session.session.close())
  def query(driver: ZIO[Scope, Throwable, Driver], q: Query) = {
    for {
      d <- driver  
      session <- ZIO.acquireRelease(acquire(d))(release(_))
      res <- ZIO.attempt(session.session.run(Query.toNeoQuery(q)))
    } yield res 
  }
}
