package drawtogether.client

import drawtogether.client.services.ConnectionService.WebSocketMessages.WebSocketConnected
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.ui.HeaderToolbar
import drawtogether.shared.communication.SharedMessages.SLoginSuccessFul
import org.scaloid.common._

/** this activity is shown if the user is offline or has connection problems */
class OfflineActivity extends DrawTogetherActivity with HeaderToolbar {

  /** id of xml layout file */
  val layoutId: Int = R.layout.offline_layout

  /** reconnect button */
  lazy val reconnect = findView(TR.btn_reconnect)

  onCreate {
    // try to connect to the server again
    reconnect.onClick(conn(_.reconnect()))
  }

  setOnReceive {
    case WebSocketConnected() => startActivity(SIntent[StartActivity])
    case SLoginSuccessFul()   => startActivity(SIntent[StartActivity])
  }
}



