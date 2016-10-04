package actors.ingame

import java.util.Base64

import actors.InternalMessages._
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import drawing.ServerDrawing
import drawing.ServerDrawing.ServerDrawImage
import drawtogether.shared.communication.SharedMessages._
import drawtogether.shared.drawing.Drawing2Image
import drawtogether.shared.drawing.DrawingObjects.DrawCommand
import drawtogether.shared.ingame.InGame._
import models.Tables.DrawingsRow
import models.{DatabaseAccess, DrawingsAccess}
import play.Logger

import scala.collection.mutable
import scala.concurrent.duration._

/** an actor that manages a game - for each running game there is one channel */
class InGameActor(gameId: String, serverActor: ActorRef, gameSettings: GameSettings) extends Actor with DatabaseAccess with DrawingsAccess {

  // add global execution context
  import scala.concurrent.ExecutionContext.Implicits.global

  /** current users in this channel */
  var joinedUsers = Map[ActorRef, ServerUser]()

  /** list of users who have played in this game */
  var contributedUsers = Set[ServerUser]()

  /** the commands send in this game totally */
  var commandList = mutable.ArrayBuffer[DrawCommand]()

  /** the current state the game is in - starting with waiting */
  var currentState: GameState = WaitingState

  /** the server representation of the image that is drawn, used as cache for requests */
  val serverImage = ServerDrawImage.createEmptyImage()

  /** a counter that gets increased with each round */
  var roundNumber = 0

  /** sends a message to all connected clients with optional excluding one user */
  def broadCast(message: NetworkMessage, excluding: Option[ActorRef] = None): Unit = {
    joinedUsers
      .keys
      .filterNot(excluding.contains(_)) // remove user - if option is valid and contains the user
      .foreach(_ ! message)
  }


  /** this timer counts down the secondsPerRound and then goes to the next state */
  var currentTimer: Option[Cancellable] = Some(context.system.scheduler.schedule(0.seconds, 1.seconds) {
    currentState match {
      case WaitingState => // do nothing

      case DoneState => // do nothing

      case UserDrawingState(user, remainingSeconds) if remainingSeconds > 0 =>
        currentState = UserDrawingState(user, remainingSeconds - 1)
        broadCast(SGameState(currentState, roundNumber))

      case UserDrawingState(user, remainingSeconds) if remainingSeconds <= 0 =>
        roundNumber += 1
        if (roundNumber >= gameSettings.totalRounds) {
          currentState = DoneState
          broadCast(SGameState(currentState, roundNumber))

          // store the game
          val bufImage = ServerDrawing.drawingToImage(serverImage)
          val buffer = ServerDrawing.imageToJPGByteArray(bufImage)
          storeDrawing(
            DrawingsRow(
              gameId,
              gameSettings.title,
              gameSettings.totalRounds,
              gameSettings.secondsPerRound,
              Base64.getEncoder.encodeToString(buffer),
              System.currentTimeMillis / 1000
            )
          )
          // store the contributors
          storeContributors(gameId, contributedUsers.toList)
          // kill this actor
          serverActor ! InternalGameKilled(gameId)
          currentTimer.map(_.cancel())
          context.stop(self)
        } else {
          currentState = UserDrawingState(findNextUser(), gameSettings.secondsPerRound)
          broadCast(SGameState(currentState, roundNumber))
        }

    }
  })

  /** logic that tries to find the next user */
  def findNextUser(): User = {
    /** internal function to prevent code duplication */
    def getNextFromList(currentUser: User): User = {
      val allUsers = joinedUsers.values.toList
      val index = allUsers.map(_.name).indexOf(currentUser.name)
      val userName = allUsers.lift(index + 1).getOrElse(allUsers.head)
      userName.toUser
    }
    currentState match {
      // this is called if the user finished his drawing and the next user is due
      case UserDrawingState(user, _) => getNextFromList(user)
      // as only one player is in the game, we take him
      case WaitingState => joinedUsers.values.head.toUser
      // this should never happen
      case DoneState => Logger.error("nextUser on Done state"); joinedUsers.values.head.toUser
    }
  }


  def receive: Receive = {

    // sends an imageBuffer of the current screen content
    case InternalImageBufferRequest(_) =>
      val bufImage = ServerDrawing.drawingToImage(serverImage)
      sender ! ServerDrawing.imageToJPGByteArray(bufImage)

    // an status of our actor was requested, send it out to the sender
    case InternalGameStatusRequest() =>
      val bufImage = ServerDrawing.drawingToImage(serverImage)
      val resizedBuffer = ServerDrawing.imageToJPGByteArray(ServerDrawing.resizeImage(bufImage))
      sender ! InGamePreview(gameId, gameSettings, joinedUsers.size, roundNumber, Base64.getEncoder.encodeToString(resizedBuffer))

    case InternalTryUserJoinRequest(serverUser, userActor) =>
      //check user amount and accept or reject
      val userCountBeforeJoin = joinedUsers.size
      if (userCountBeforeJoin >= 4) {
        // game is full
        sender ! InternalTryUserJoinResponse(false)
      } else {
        // game is not full, join the player
        joinedUsers += userActor -> serverUser
        if (joinedUsers.size > 1 && currentState == WaitingState) {
          // game was waiting for players, so start the game (again)
          currentState = UserDrawingState(findNextUser(), gameSettings.secondsPerRound)
        }
        // prepair current game image
        val bufImage = ServerDrawing.drawingToImage(serverImage)
        val resizedBuffer = ServerDrawing.imageToPNGByteArray(bufImage)
        sender ! InternalTryUserJoinResponse(true, Some(Base64.getEncoder.encodeToString(resizedBuffer)))
        // broadcast the status for all
        broadCast(SGameState(currentState, roundNumber))
        // broadcast join
        broadCast(SGameUserList(joinedUsers.map(_._2.toUser).toList))
      }

    // all messages from a logged in user to the game
    case IAuthedMessage(serverUser, packet) => packet match {

      case CGameLeaveRequest() =>
        // remove user from list
        joinedUsers = joinedUsers.filter(x => x._2 != serverUser)
        // tell other player
        broadCast(SGameUserList(joinedUsers.map(_._2.toUser).toList))
        // set state to waiting if userLength is < 2
        if (joinedUsers.size < 2) {
          currentState = WaitingState
        }
        currentState = currentState match {
          case UserDrawingState(user, i) if user == serverUser.toUser => UserDrawingState(findNextUser(), i)
          case x                                                      => x
        }
        broadCast(SGameState(currentState, roundNumber))
        // check if all gone
        if (joinedUsers.isEmpty) {
          // kill itself
          serverActor ! InternalGameKilled(gameId)
          currentTimer.map(_.cancel())
          context.stop(self)
        }

      case CGameDrawCommand(cmd) =>
        // check if it's the users turn
        currentState match {
          case UserDrawingState(drawingUser, _) if drawingUser == serverUser.toUser =>
            // save command
            commandList += cmd
            // apply the command on the server
            Drawing2Image.applyToDrawing(serverImage, cmd)
            // send update to all other players - excluding sender
            broadCast(SGameDrawCommand(cmd), Some(sender()))
            // append user to contributors
            contributedUsers += serverUser
          case s                                                                    =>
            Logger.error(s"DrawEvent not valid - $serverUser tried to draw but state is $s")
        }
    }

    case e: InternalServerMessage => Logger.error(s"did not catch that - $e")
    case e                        => Logger.error(s"ERROR - Packet not authed - $e")
  }
}

object InGameActor {
  // best practice: http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  def props(id: String, serverActor: ActorRef, gameSettings: GameSettings): Props = Props(new InGameActor(id, serverActor, gameSettings))
}