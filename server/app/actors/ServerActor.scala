package actors

import java.util.concurrent.TimeUnit

import actors.InternalMessages.{InternalImageBufferRequest, _}
import actors.ingame.InGameActor
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import drawtogether.shared.communication.SharedMessages.{CCreateGameRequest, CGameLeaveRequest, CHighScoreDrawingsRequest, CJoinGameRequest, CLoginRequest, COriginalDrawingRequest, CRecentDrawingsRequest, CRegisterRequest, CRunningGameListRequest, CStaredRequest, CUserDisconnected, CUserDrawingsRequest, GameMessage, SHighScoreDrawingsResponse, SJoinGameRejected, SJoinGameSuccessful, SLoginFailed, SLoginSuccessFul, SNotAuthorized, SOriginalDrawingResponse, SRecentDrawingsResponse, SRegisterFailed, SRegisterSuccessFul, SRunningGameListResponse, SUserDrawingsResponse}
import drawtogether.shared.ingame.InGame._
import models.{DatabaseAccess, DrawingsAccess, LoginFunctions}
import play.api.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * This is the header Actor on the server
  * every message should go through this single actor
  * Here we also keep the state (as refs to other actors)
  */
class ServerActor extends Actor with DatabaseAccess with LoginFunctions with DrawingsAccess {

  // allow advanced future feature
  import util.FutureEnhancements.FutureCompanionOps

  // add global execution context
  import scala.concurrent.ExecutionContext.Implicits.global

  // set timeout for async ask
  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  /** a map containing all users that are currently in the system */
  var connectedUsers = Map[ServerUser, ActorRef]()

  /** a list of currently open games */
  var openGames = Map[String, (ActorRef, GameSettings)]()

  /** a map from a userActor to a channelActor */
  var usersJoinedChannels = Map[ServerUser, ActorRef]()

  /** helper function that is used to only forword messages if user actor has successfully logged in */
  def ifLoggedIn(runIfLoggedIn: (ServerUser => Unit))(implicit context: ActorContext) = {
    val mySender = context.sender()
    connectedUsers.map(_.swap).get(mySender) match {
      case Some(x) => runIfLoggedIn(x)
      case None    =>
        Logger.error("ifLoggedIn failed")
        mySender ! SNotAuthorized(s"You are not logged in - $mySender")
    }
  }

  def receive: Receive = {

    // request for the current image buffer
    case m@InternalImageBufferRequest(id) =>
      openGames.get(id).foreach(_._1.forward(m))


    // game got killed, remove it from list
    case InternalGameKilled(id) =>
      val inGameActor = openGames(id)
      usersJoinedChannels = usersJoinedChannels.filterNot(_._2 == inGameActor)
      openGames = openGames.filterNot(_._1 == id)

    // user requested a register
    case CRegisterRequest(username, password) =>
      val currentSender = sender()
      addUser(username, password).map {
        case Right(_) => currentSender ! SRegisterSuccessFul(username)
        case Left(m)  => currentSender ! SRegisterFailed(m)
      }

    // user requested a login
    case InternalLoginUser(user) =>
//      if (connectedUsers.contains(user)) {
//        connectedUsers = connectedUsers.filterNot(_._1 == user)
//        usersJoinedChannels(user) ! IAuthedMessage(user, CGameLeaveRequest())
//        usersJoinedChannels = usersJoinedChannels.filterNot(_._1 == user)
//      }
      connectedUsers += user -> sender
      sender ! SLoginSuccessFul()

    // user requested a login
    case CLoginRequest(username, password) =>
      val currentSender = sender()
      tryLogin(username, password).map {
        case None       => currentSender ! SLoginFailed("wrong username or password")
        case Some(user) =>
//          if (connectedUsers.contains(user)) {
//            connectedUsers = connectedUsers.filterNot(_._1 == user)
//            usersJoinedChannels(user) ! IAuthedMessage(user, CGameLeaveRequest())
//            usersJoinedChannels = usersJoinedChannels.filterNot(_._1 == user)
//          }
          connectedUsers += user -> currentSender
          currentSender ! SLoginSuccessFul()
      }

    case COriginalDrawingRequest(gameId) =>
      val currentSender = sender()
      loadImageForGameId(gameId).map {
        case Some(base64Image) => currentSender ! SOriginalDrawingResponse(gameId, base64Image)
        case None              => Logger.error("unknown game id")
      }


    case CUserDisconnected() =>
      ifLoggedIn(user => {
        connectedUsers = connectedUsers.filterNot(_._2 == sender())
        usersJoinedChannels(user) ! IAuthedMessage(user, CGameLeaveRequest())
        usersJoinedChannels = usersJoinedChannels.filterNot(_._2 == sender())
      })

    // the user requested to create a new game, so we start a new actor
    case CCreateGameRequest(settings) =>
      ifLoggedIn(user => {
        // create new gameId
        val newId = newUUID()
        // create an actor for the new game
        val gameActor = context.actorOf(InGameActor.props(newId, self, settings), s"game-$newId")
        // let the user join the game
        gameActor ? InternalTryUserJoinRequest(user, sender())
        // add the game to openGames
        openGames += newId ->(gameActor, settings)
        // add user to game
        usersJoinedChannels += user -> gameActor
        sender ! SJoinGameSuccessful(newId, settings, None)
      })


    case CStaredRequest(toStar, drawingId) =>
      ifLoggedIn(serverUser => {
        toStar match {
          case false => unStarDrawing(serverUser, drawingId)
          case true  => starDrawing(serverUser, drawingId)
        }
      })

    // user requested the list of currently played games
    case CRunningGameListRequest() =>
      ifLoggedIn(serverUser => {
        // needed as we run the task async and then the sender is gone
        val currentSender = sender()
        // ask every openGame about the InternalGameStatus
        val requestFutures = openGames.values.toList.map(g => (g._1 ? InternalGameStatusRequest()).asInstanceOf[Future[InGamePreview]])
        // convert the list of futures to a future list of options
        val futureList = Future.allAsOptions(requestFutures)
        // once the future is done
        futureList.andThen({
          // flatten the list to remove all failures and send it to the client
          case Success(list) => currentSender ! SRunningGameListResponse(list.flatten)
          case Failure(s)    => Logger.error(s.toString)
        })
      })

    case CHighScoreDrawingsRequest() =>
      val currentSender = sender()
      ifLoggedIn(user => {
        applyUserDrawingStars(user, loadBestFinishedDrawings()).map(x =>
          currentSender ! SHighScoreDrawingsResponse(x.toList)
        )
      })
    //recent drawings
    case CRecentDrawingsRequest() =>
      val currentSender = sender()
      ifLoggedIn(user => {
        applyUserDrawingStars(user, loadRecentFinishedDrawings()).map(x =>
          currentSender ! SRecentDrawingsResponse(x.toList)
        )
      })

    case CUserDrawingsRequest() =>
      val currentSender = sender()
      ifLoggedIn(user => {
        applyUserDrawingStars(user, loadUserDrawings(user)).map(x =>
          currentSender ! SUserDrawingsResponse(x.toList)
        )
      })

    // duplication of CRunningGameListRequest without auth
    case InternalRunningGameListRequest() =>
      val currentSender = sender()
      val requestFutures = openGames.values.toList.map(g => (g._1 ? InternalGameStatusRequest()).asInstanceOf[Future[InGamePreview]])
      val futureList = Future.allAsOptions(requestFutures)
      futureList.andThen({
        case Success(list) => currentSender ! SRunningGameListResponse(list.flatten)
        case Failure(s)    => Logger.error(s.toString)
      })


    // user tries to join a game
    case m@CJoinGameRequest(id) =>
      ifLoggedIn(serverUser => {
        val currentSender = sender()
        openGames.get(id) match {
          case Some(gameActor) =>
            (gameActor._1 ? InternalTryUserJoinRequest(serverUser, sender())).asInstanceOf[Future[InternalTryUserJoinResponse]] map {
              case InternalTryUserJoinResponse(true, image) =>
                usersJoinedChannels += serverUser -> gameActor._1
                currentSender ! SJoinGameSuccessful(id, gameActor._2, image)
              case InternalTryUserJoinResponse(false, _)    =>
                currentSender ! SJoinGameRejected("server full")
            }
          case None            => currentSender ! SNotAuthorized("no valid gameId")
        }
      })

    case m: CGameLeaveRequest =>
      ifLoggedIn(serverUser => {
        usersJoinedChannels.get(serverUser) match {
          case Some(game) => game forward IAuthedMessage(serverUser, m)
          case None       => sender ! SNotAuthorized("did not join a channel before sending objects")
        }
        usersJoinedChannels = usersJoinedChannels.filterNot(_._2 == sender())
      })

    // user send us a instance of gameMessage, if he is logged in and joined a channel, we forward it
    case m: GameMessage =>
      ifLoggedIn(serverUser => {
        usersJoinedChannels.get(serverUser) match {
          case Some(game) => game forward IAuthedMessage(serverUser, m)
          case None       =>
            Logger.error("gameMessage without joining game")
            sender ! SNotAuthorized("did not join a channel before sending objects")
        }
      })

    case x => Logger.error("Got Message" + x)
  }
}
