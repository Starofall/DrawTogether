package actors.ingame

import akka.actor.{Actor, ActorRef, Props}
import drawtogether.shared.communication.SharedMessages.{CGameDrawCommand, CJoinGameRequest, CLoginRequest, CRegisterRequest, SGameState, SJoinGameRejected, SLoginFailed, SLoginSuccessFul, SRegisterFailed, SRegisterSuccessFul}
import drawtogether.shared.drawing.DrawingObjects.{DrawColor, DrawCommand}
import drawtogether.shared.ingame.InGame.{DoneState, UserDrawingState, WaitingState}
import play.api.Logger

import scala.util.Random

/** we used this actor as a dummy player to play the game with and test the protocol */
class TestUserActor(id: String, gameToJoin: String, mainActor: ActorRef, userName: String, password: String) extends Actor {

  Logger.error(s"$id - 1 - register myself")
  mainActor ! CRegisterRequest(userName, password)

  def receive: Receive = {

    case SRegisterFailed(reason) =>
      Logger.error(s"$id - 1f - register failed $reason")

    case SRegisterSuccessFul(_) =>
      Logger.error(s"$id - 1r - register successful")
      Logger.error(s"$id - 2 - try login myself")
      mainActor ! CLoginRequest(userName, password)

    case SLoginFailed(reason) =>
      Logger.error(s"$id - 2f - login failed $reason")

    case SLoginSuccessFul() =>
      Logger.error(s"$id - 2r - login worked")
      Logger.error(s"$id - 3 - try game join")
      mainActor ! CJoinGameRequest(gameToJoin)

    case SJoinGameRejected(reason) =>
      Logger.error(s"$id - 3f - join failed")

    case SGameState(state, roundNumber) =>
      Logger.error(s"$id - 4 - got gamestate $state")
      state match {
        case WaitingState              => Logger.error(s"$id - 4 - do nothing")
        case DoneState                 => Logger.error(s"$id - 4 - i should not be here - its done")
        case UserDrawingState(user, _) => if (user.name == userName) {
          mainActor ! CGameDrawCommand(DrawCommand(Random.nextInt(200), Random.nextInt(200), Random.nextInt(200), Random.nextInt(200), useBresenham = true, 10, DrawColor.GREEN))
        } else {
          Logger.error(s"$id - 4 - other user is drawing - waiting")
        }
      }
  }
}

object TestUserActor {
  // best practice: http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  def props(id: String, gameToJoin: String, mainActor: ActorRef, userName: String, password: String): Props = Props(new TestUserActor(id, gameToJoin, mainActor, userName, password))
}