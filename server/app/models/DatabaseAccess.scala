package models

import java.sql.DriverAction

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.{Effect, NoStream}
import slick.driver.JdbcProfile

/**
  * Prepare everything needed for database access and offers some simple functions
  */
trait DatabaseAccess {

  // offer a default executionContext using play
  implicit val executionContext =  play.api.libs.concurrent.Execution.Implicits.defaultContext

  // needed for slick
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  implicit val myConfig = dbConfig.db
  import dbConfig.driver.api._

  /** makes db available */
  val db = dbConfig.db

  /** creates a UUID for IDs */
  def newUUID() = java.util.UUID.randomUUID.toString

  // needed for forwards
  import scala.language.higherKinds

  /** forward query to slick */
  def runQuery[F, T, D[_]](q: slick.lifted.Query[F, T, D]) = db.run(q.result)

  /** forward query to slick */
  def runQuerySingle[F, T, D[_]](q: slick.lifted.Query[F, T, D]) = db.run(q.result.headOption)

  /** forward query to slick */
  def runInsert[R, E <: Effect](q: dbio.DBIOAction[R, NoStream, E]) = db.run(q)
}
