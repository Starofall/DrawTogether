package models

import actors.InternalMessages.ServerUser
import drawtogether.shared.ingame.InGame.{FinishedDrawing, GameSettings, User}
import models.Tables._
import play.api.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by info on 18.06.2016.
  */
trait DrawingsAccess {
  self: DatabaseAccess =>

  import Tables._

  // enhance future
  import dbConfig.driver.api._
  import util.FutureEnhancements.FutureExtensions


  /** store a drawing row in the database */
  def storeDrawing(f: DrawingsRow): Unit = {
    runInsert(Drawings += f)
  }

  /** stores contributors for a given drawing */
  def storeContributors(drawingId: String, contributorList: List[ServerUser]): Unit = {
    val inserts = contributorList.map(s => DrawingContributesRow(drawingId, s.userId))
    runInsert(DrawingContributes ++= inserts)
  }

  /** apply the staring of a user to a future of Seq[FinishedDrawing] */
  def applyUserDrawingStars(serverUser: ServerUser, futureDrawings: Future[Seq[FinishedDrawing]]): Future[Seq[FinishedDrawing]] = {
    for (
      userDrawings <- getUserStaredDrawingIds(serverUser);
      drawings <- futureDrawings;
      drawingsWithUserStaring <- Future(drawings.map(t => t.copy(userStared = userDrawings.contains(t.gameId))))
    ) yield drawingsWithUserStaring
  }

  /** returns a list of all drawings user stared */
  def getUserStaredDrawingIds(serverUser: ServerUser): Future[Seq[String]] = {
    runQuery(DrawingStars.filter(_.userId === serverUser.userId).map(_.drawingId).distinct)
  }

  /** returns the best drawings */
  def loadBestFinishedDrawings(): Future[Seq[FinishedDrawing]] = {
    // query to search for the game with the most drawingStars
    val groupedDrawingStars = DrawingStars.groupBy(_.drawingId).map(x => (x._1 /* id */ , x._2.length /* count */ ))
    val drawingsWithStars = Drawings join groupedDrawingStars on (_.id === _._1)
    val query = drawingsWithStars.sortBy(_._2._2.desc.nullsLast).take(20)
    runQuery(query).mapAll {
      case Success(s) => s.map(x =>
        FinishedDrawing(
          x._1.id,
          GameSettings(x._1.title, x._1.totalRounds, x._1.secondsPerRound),
          x._2._2,
          x._1.base64PngImage)
      )
      case Failure(e) => println(e); List()
    }
  }

  /** returns the most recent drawings */
  def loadRecentFinishedDrawings(): Future[Seq[FinishedDrawing]] = {
    // query to search for the game with the most drawingStars
    val groupedDrawingStars = DrawingStars.groupBy(_.drawingId).map(x => (x._1, x._2.length))
    val drawingsWithStars = Drawings joinLeft groupedDrawingStars on (_.id === _._1)
    val query = drawingsWithStars.sortBy(_._1.creationTimestamp.desc.nullsLast).take(20)
    runQuery(query).mapAll {
      case Success(s) => s.map(x =>
        FinishedDrawing(
          x._1.id,
          GameSettings(x._1.title, x._1.totalRounds, x._1.secondsPerRound),
          // as this is an option, we need to provide an alternative -> 0
          x._2.map(_._2).getOrElse(0),
          x._1.base64PngImage)
      )
      case Failure(e) => println(e); List()
    }
  }

  /** returns the drawings for a user */
  def loadUserDrawings(serverUser: ServerUser): Future[Seq[FinishedDrawing]] = {
    // query to search for the game with the most drawingStars
    val userContributions = DrawingContributes.filter(_.userId === serverUser.userId)
    val groupedDrawingStars = DrawingStars.groupBy(_.drawingId).map(x => (x._1, x._2.length))
    val drawingsWithStarsAndContributions = Drawings joinLeft groupedDrawingStars on (_.id === _._1) join userContributions on (_._1.id === _.drawingId)
    val query = drawingsWithStarsAndContributions.sortBy(_._1._1.creationTimestamp.desc.nullsLast).take(20)
    runQuery(query).mapAll {
      case Success(s) => s.map(x => {
        val drawingsRow = x._1._1
        val starCountOption = x._1._2.map(_._2)
        FinishedDrawing(
          drawingsRow.id,
          GameSettings(drawingsRow.title, drawingsRow.totalRounds, drawingsRow.secondsPerRound),
          // as this is an option, we need to provide an alternative -> 0
          starCountOption.getOrElse(0),
          drawingsRow.base64PngImage)
      })
      case Failure(e) => println(e); List()
    }
  }

  /** loads an image for a gameId */
  def loadImageForGameId(gameId: String): Future[Option[String]] = {
    val query = Drawings.filter(_.id === gameId)
    runQuerySingle(query).mapAll {
      case Success(s) => s.map(_.base64PngImage)
      case Failure(e) => println(e); None
    }
  }

  /** stars an image for given user and id */
  def starDrawing(user: ServerUser, drawingId: String): Unit = {
    runInsert(DrawingStars += DrawingStarsRow(drawingId, user.userId))
  }

  /** unstars an image for given user and id */
  def unStarDrawing(user: ServerUser, drawingId: String): Unit = {
    db.run(DrawingStars.filter(f => f.drawingId === drawingId && f.userId === user.userId).delete)
  }
}