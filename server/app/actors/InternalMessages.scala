package actors

import akka.actor.ActorRef
import drawtogether.shared.communication.SharedMessages.ActorMessage
import drawtogether.shared.ingame.InGame.User

/**
  * Created by info on 06.06.2016.
  */
object InternalMessages {

  /** Group all serverInternalMessages together */
  trait InternalServerMessage extends ActorMessage

  /** this message is send to a InGameActor to gain its internal state for previewing in list */
  case class InternalGameStatusRequest() extends InternalServerMessage

  /** internal variant to get all running games withouth auth needed */
  case class InternalRunningGameListRequest() extends InternalServerMessage

  /** if the user logged in with a token, we accept the user */
  case class InternalLoginUser(user: ServerUser) extends InternalServerMessage

  /** this method returns the full imageBuffer of the current game */
  case class InternalImageBufferRequest(id: String) extends InternalServerMessage

  /** a game got killede */
  case class InternalGameKilled(id: String) extends InternalServerMessage

  /** a wrapper for messages that also contains information about the logged in user */
  case class IAuthedMessage(user: ServerUser, packet: ActorMessage)

  case class ServerUser(name: String, userId: String) {
    def toUser = User(name)
  }

  // joining
  case class InternalTryUserJoinRequest(user: ServerUser, actorRef: ActorRef) extends InternalServerMessage
  case class InternalTryUserJoinResponse(success: Boolean, base64png: Option[String] = None) extends InternalServerMessage
}
