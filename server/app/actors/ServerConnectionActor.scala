package actors

import actors.InternalMessages.{InternalLoginUser, ServerUser}
import akka.actor.{Cancellable, _}
import drawtogether.shared.communication.SharedMessages.{CKeepAlive, CUserDisconnected, ClientToServer, NetworkMessage, PicklerTrait, SKeepAlive, ServerToClient}
import play.api.Logger
import prickle.{Pickle, Unpickle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * For each open webSocket one of this actors is created.
  * Every message that comes from the webSocket passes this actor.
  * Messages to the actor are either send to the webSocket or to the MainServerActor.
  */
class ServerConnectionActor(out: ActorRef, mainServerActor: ActorRef, preAuthedUser: Option[ServerUser]) extends Actor with ActorLogging with PicklerTrait {

  // if we have a preauthed user, notify main server actor about this
  preAuthedUser.foreach(f => {
    Logger.error("Got a preAutheduser:" + f)
    mainServerActor ! InternalLoginUser(f)
  })

  /** the last time we got a keepAlive from the client */
  var lastResponseTime = System.currentTimeMillis() / 1000

  /**
    * Pings the ClientConnectionActor every 'sendTime' seconds
    * If he doesn't ping back in 'maxTime' seconds, we lost the connection
    * Lets all the other rooms know and kills himself
    **/
  val keepAlive: Cancellable = context.system.scheduler.schedule(0.seconds, 45.seconds) {
    self ! SKeepAlive()
    if (System.currentTimeMillis() / 1000 - lastResponseTime >= 60) {
      keepAlive.cancel()
      mainServerActor ! CUserDisconnected()
      //this prevents that in the last run there could be an nullPointer
      if (context != null && self != null) {
        Logger.error(s"My Client ${context.self.toString()} didn't send anything for ${System.currentTimeMillis() / 1000 - lastResponseTime}s...")
        keepAlive.cancel()
        context.stop(self)
      }
    }
  }

  /** forward each message to the serverActor */
  def receive = {

    case msg: String => Unpickle[NetworkMessage].fromString(msg) match {
      case Failure(exception) => Logger.error("UNKNOWN PACKET FORMAT")
      case Success(value)     => self ! value
    }

    // messages from the client go the the mainServerActor
    case m: ClientToServer =>
      m match {
        case CKeepAlive() => lastResponseTime = System.currentTimeMillis() / 1000
        case _            =>
          Logger.error("ServerConnectionActor-fromClient - " + m)
          mainServerActor ! m
      }

    // messages to the client go out through the backChannel
    case n: ServerToClient =>
      try {
        out ! Pickle.intoString(n: NetworkMessage)
        if (!n.isInstanceOf[SKeepAlive]) {
          Logger.error("ServerConnectionActor-toClient   - " + n)
        }
      } catch {
        case e: Throwable => Logger.error(s"PRICKLE - $e")
      }

    case x => Logger.error("Unknown packet in ConnectionHandlerActor - " + x)
  }

}

object ServerConnectionActor {
  def props(backChannel: ActorRef, mainServerActor: ActorRef, preAuthedUser: Option[ServerUser]): Props = Props(new ServerConnectionActor(backChannel, mainServerActor, preAuthedUser))
}

