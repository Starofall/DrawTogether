package drawtogether.client.services

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import drawtogether.client.services.ConnectionService.NetworkListener
import drawtogether.client.services.ConnectionService.WebSocketMessages.{WebSocketClosed, WebSocketError}
import drawtogether.client.util.DrawTogetherPrefs
import drawtogether.client.{OfflineActivity, R, TypedFindView}
import drawtogether.shared.communication.SharedMessages.{CLoginRequest, ClientToServer, NetworkMessage, SNotAuthorized, ServerToClient}
import org.scaloid.common._

/**
  * A trait for all activites used in the application
  */
trait DrawTogetherActivity extends AppCompatActivity with SActivity with SContext with TypedFindView with NetworkListener {

  // make executionContext available to all activities
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  val layoutId: Int

  onCreate {
    // this is temp as at the end each activity should have a valid xml layout
    if (layoutId != 0) {
      setContentView(layoutId)
    }
  }

  // connect to tall services
  val conn = new LocalServiceConnection[ConnectionService]
  val data = new LocalServiceConnection[LocalDataService]

  /** this is a forward function to the localOnRecive function that can be changed at runtime */
  def onReceive: PartialFunction[Any, Unit] = {
    case SNotAuthorized(_)       => DrawTogetherPrefs.loadSavedLoginData().foreach(l => sendMessage(CLoginRequest(l.username, l.password)))
    case WebSocketError(reason)  => runOnUiThread(startActivity(SIntent[OfflineActivity]))
    case WebSocketClosed(reason) => runOnUiThread(startActivity(SIntent[OfflineActivity]))
    case x                       => if (localOnReceive.isDefinedAt(x)) {
      runOnUiThread(localOnReceive(x))
    } else {
      error(s"Unknown Packet in recieve: $x")
    }
  }

  /** the runtime onRecieve function */
  var localOnReceive: PartialFunction[Any, Unit] = {
    case _ => error(getString(R.string.local_on_recieve))
  }

  /** sets a update the onReceive for this local activity */
  def setOnReceive(newReceive: PartialFunction[Any, Unit]): Unit = {
    localOnReceive = newReceive
  }

  /** start all services */
  onResume {
    // define the current activity as the now relevant network listener
    conn(c => c.setNetworkListener(this))
    startService(new Intent(getApplicationContext, classOf[ConnectionService]))
    startService(new Intent(getApplicationContext, classOf[LocalDataService]))
  }

//  /** stop services */
//  override def onUserLeaveHint(): Unit = {
//    sendMessage(CUserDisconnected())
//    stopService(new Intent(getApplicationContext, classOf[ConnectionService]))
//    stopService(new Intent(getApplicationContext, classOf[LocalDataService]))
//  }

  // forwards to service
  def sendMessage(msg: ClientToServer): Unit = {
    conn(c => c.sendMessage(msg))
  }

  def fakeReceive(msg: ServerToClient): Unit = {
    conn(c => c.fakeReceive(msg))
  }

  def postponeForNextActivity(msg:NetworkMessage): Unit = {
//    error(s"Pospone: $msg")
    conn(c => c.postponeForNextActivity(msg))
  }

  /** gets the user name */
  def loadUsername(): String = {
    var userName = ""
    DrawTogetherPrefs.loadSavedLoginData().foreach(ld => {
      userName = ld.username
    })
    userName
  }
}
