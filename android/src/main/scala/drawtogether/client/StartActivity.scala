package drawtogether.client

import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.DrawTogetherPrefs
import drawtogether.shared.communication.SharedMessages.{CLoginRequest, SLoginFailed, SLoginSuccessFul}
import org.scaloid.common._

/** This activity is loaded after starting the application and it either forwards the user to login or to the channelList */
class StartActivity extends DrawTogetherActivity {

  /** invalid id - as this is never shown */
  val layoutId: Int = 0

  setOnReceive {
    case SLoginSuccessFul() => startActivity(SIntent[ChannelListActivity])
    case SLoginFailed(e)    => startActivity(SIntent[LoginActivity])
  }

  onResume {
    DrawTogetherPrefs.loadSavedLoginData() match {
      case None    => startActivity(SIntent[LoginActivity])
      case Some(x) => sendMessage(CLoginRequest(x.username, x.password))
    }
  }
}



