package drawtogether.client.services

import java.net.URI

import android.util.Log
import drawtogether.client.services.ConnectionService.NetworkListener
import drawtogether.client.services.ConnectionService.WebSocketMessages._
import drawtogether.client.util.DrawTogetherPrefs
import drawtogether.shared.communication.SharedMessages
import drawtogether.shared.communication.SharedMessages.{CKeepAlive, NetworkMessage, PicklerTrait, SKeepAlive}
import drawtogether.shared.settings.SharedSettings
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.scaloid.common.{LocalService, _}
import prickle.{Pickle, Unpickle}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


/** A android service providing abstraction for the webSocket connection */
class ConnectionService extends LocalService with PicklerTrait {

  /** this is the activity the gets all the events that come in from the network */
  private var currentListener: Option[NetworkListener] = None

  /** stores a message for the next activity */
  private val accumulation = mutable.ArrayBuffer[SharedMessages.NetworkMessage]()

  /** create webSocket connection and hook into the results */
  private var client: WebSocketClient = null // null is acceptable here, as we are sure to directly create a client

  /** called when the service is created. connects to the server */
  override def onCreate(): Unit = {
    client = createWebSocketClient()
    client.connect()
  }

  /** reconnect to the webSocket connection */
  def reconnect(): Unit = {
    client = createWebSocketClient()
    client.connect()
  }

  /** sets a new networkListener to handle all incoming messages */
  def setNetworkListener(networkListener: NetworkListener): Unit = {
    currentListener = Some(networkListener)
    // if there are any messages in the accumulation, send them here
    accumulation.foreach(fakeReceive)
    accumulation.clear()
  }

  /** this function fakes the receive of a server message */
  def fakeReceive(msg: NetworkMessage): Unit = {
    currentListener.foreach(_.onReceive(msg))
  }

  /** stop delivering messages until next activity */
  def postponeForNextActivity(msg: NetworkMessage): Unit = {
    accumulation.append(msg)
  }

  /** sends the NetworkMessage to the server */
  def sendMessage(msg: NetworkMessage): Unit = {
    // as serialization takes time, we put this in an future
    Future {
      try {
        client.send(Pickle.intoString(msg: NetworkMessage))
        Log.e("OUT-", msg.toString)
      } catch {
        case e: Throwable =>
          Log.e("SOCKET", s"SEND ERROR: $e")
          currentListener.foreach(_.onReceive(WebSocketClosed("offline")))
      }
    }
  }

  /** returns a new webSocket connection defined for the server */
  def createWebSocketClient(): WebSocketClient = {
    // if login data is available, use them inside the request to login automatically
    val tokens = DrawTogetherPrefs.loadSavedLoginData() match {
      case Some(x) => s"?username=${x.username}&password=${x.password}"
      case None    => "" // no header
    }
    val url = new URI(SharedSettings.SERVER_URL + tokens)
    // create object
    new WebSocketClient(url) {

      def onOpen(handShakedata: ServerHandshake): Unit = {
        Log.e("CON-", "onOpen")
        currentListener.foreach(_.onReceive(WebSocketConnected()))
        sendMessage(CKeepAlive())
      }

      def onError(ex: Exception): Unit = {
        Log.e("CON-", s"onError: ${ex.getMessage}")
        currentListener.foreach(_.onReceive(WebSocketError(ex.toString)))
      }

      def onClose(code: Int, reason: String, remote: Boolean): Unit = {
        Log.e("CON-", "onClose")
        currentListener.foreach(_.onReceive(WebSocketClosed(reason)))
      }

      def onMessage(message: String): Unit = {
        Unpickle[NetworkMessage].fromString(message) match {
          case Failure(exception) => currentListener.foreach(_.onReceive(WebSocketError(exception.toString)))
          case Success(value)     =>
            Log.e("IN -", value.toString)
            // we manage keepAlive's on our own
            if (value.isInstanceOf[SKeepAlive]) {
              sendMessage(CKeepAlive())
            } else {
              currentListener.foreach(_.onReceive(value))
            }
        }
      }

    }
  }


  // Log when the ConnectionService get destroyed
  override def onDestroy(): Unit = {
    super.onDestroy()
    error("ConnectionService got destroyed")
  }
}


object ConnectionService {

  /** messages for webSocket states */
  object WebSocketMessages {
    case class WebSocketConnected()
    case class WebSocketClosed(reason: String)
    case class WebSocketError(msg: String)
  }

  /** trait that has to be implemented by an activity who wants to receive events */
  trait NetworkListener {
    def onReceive: PartialFunction[Any, Unit]
  }
}