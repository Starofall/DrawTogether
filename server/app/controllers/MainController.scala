package controllers

import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject._

import actors.InternalMessages.{InternalImageBufferRequest, InternalRunningGameListRequest}
import actors.{ServerActor, ServerConnectionActor}
import akka.actor._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.google.inject.Inject
import drawtogether.shared.communication.SharedMessages.SRunningGameListResponse
import models.{DatabaseAccess, DrawingsAccess, LoginFunctions}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/** Handles the WebSocket connection and the ServerActor */
@Singleton
class MainController @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller with DatabaseAccess with DrawingsAccess with LoginFunctions {

  // enhance future
  import util.FutureEnhancements.FutureExtensions

  /** timeout for ask pattern */
  implicit val timeout = Timeout(4, TimeUnit.SECONDS)

  /** this is the serverActor, every massage will pass this actor */
  val serverActor = system.actorOf(Props(classOf[ServerActor]))

  /** shows an empty page */
  def main() = Action {
    Ok(views.html.main.render())
  }


  /** shows the admin page */
  def admin() = Action.async {
    // all the requests we need for the page
    val requests = for {
      runningGames <- (serverActor ? InternalRunningGameListRequest()).asInstanceOf[Future[SRunningGameListResponse]]
      bestGames <- loadBestFinishedDrawings()
      recentGames <- loadRecentFinishedDrawings()
    } yield (runningGames, bestGames, recentGames)
    // handle responses
    requests.mapAll {
      case Success(data) => Ok(views.html.admin.render(data._1.previews.take(12), data._2.take(12), data._3.take(12)))
      case Failure(e)    => Ok(s"Timeout $e")
    }
  }

  /** get access to a preview of a running game over the wb interface */
  def preview(id: String) = Action.async {
    val imageBuffer = (serverActor ? InternalImageBufferRequest(id)).asInstanceOf[Future[Array[Byte]]]
    imageBuffer.mapAll {
      case Success(data) => Ok(data).as("image/jpg")
      case Failure(e)    => Ok("Timeout")
    }
  }

  /** get access to finished images over the web interface */
  def finished(id: String) = Action.async {
    loadImageForGameId(id).map {
      case Some(x) => Ok(Base64.getDecoder.decode(x)).as("image/jpg")
      case None    => NotFound("")
    }
  }

  /**
    * this is the header entry for the webSocket connection
    * the clients connects to this and the session is connected to the ServerConnectionActor
    * this node will than handle the connection to the user
    */
  def webSocketChatEntry = WebSocket.accept[String, String] { request =>
    val user = request.getQueryString("username").getOrElse("")
    val password = request.getQueryString("password").getOrElse("")
    val loginTry = tryLogin(user, password)
    val loginResult = Await.result(loginTry, Duration.Inf)
    ActorFlow.actorRef(out => ServerConnectionActor.props(out, serverActor, loginResult))
  }
}



