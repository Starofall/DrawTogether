package models

import actors.InternalMessages.ServerUser
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * trait used to enable user adding, login to a class
  */
trait LoginFunctions {
  self: DatabaseAccess =>

  // database import
  import Tables._
  import dbConfig.driver.api._

  // enhance future
  import util.FutureEnhancements.FutureExtensions

  /** adds a new user to the database */
  def addUser(username: String, password: String): Future[Either[String, Unit]] = {
    val newUser = UsersRow(newUUID(), username.toLowerCase(), BCrypt.hashpw(password, BCrypt.gensalt()))
    runInsert(Users += newUser).mapAll {
      case Success(s) => Right(())
      case Failure(e) => Left("Username taken")
    }
  }

  /** try the login of a user using BCrypt */
  def tryLogin(username: String, password: String): Future[Option[ServerUser]] = {
    runQuerySingle(Users.filter(_.username === username.toLowerCase)).map {
      case Some(user) => BCrypt.checkpw(password, user.saltedPassword) match {
        case true => Some(ServerUser(user.username, user.id))
        case false => None
      }
      case None => None
    }
  }
}
