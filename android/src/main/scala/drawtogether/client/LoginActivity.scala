package drawtogether.client

import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.{DrawTogetherPrefs, LoginData}
import drawtogether.shared.communication.SharedMessages.{CLoginRequest, SLoginFailed, SLoginSuccessFul}
import org.scaloid.common._

/**
  * This activity deals with log in of the user
  * The login data will be stored to the preferences
  */
class LoginActivity extends DrawTogetherActivity {

  /** id of xml layout file */
  val layoutId = R.layout.login_layout

  /** field to enter the userName */
  lazy val userName = findView(TR.input_name)
  /** field to enter the Password */
  lazy val password = findView(TR.input_password)
  /** button to log in */
  lazy val loginButton = findView(TR.btn_login)
  /** link text to sign up */
  lazy val registerTextLink = findView(TR.link_signup)

  onResume {
    // read the existing data and apply them to the view
    DrawTogetherPrefs.loadSavedLoginData().foreach(ld => {
      userName.setText(ld.username)
      password.setText(ld.password)
    })
  }

  onCreate {
    // check credentials
    getWindow.setBackgroundDrawableResource(R.drawable.background)
    loginButton.onClick(
      (userName.getText.toString, password.getText.toString) match {
        case ("", _)                => userName.setError("Please enter a username!")
        case (a, _) if a.length < 4 => userName.setError("Username to short!")
        case (_, "")                => password.setError("Please enter a password!")
        case (a, b)                 => sendMessage(CLoginRequest(a, b))
      }
    )
    // onclick for register goes to sign up
    registerTextLink.onClick(startActivity(SIntent[RegisterActivity]))
  }

  setOnReceive {
    // if the login was successful go to the channel list (at the  moment running games)
    case SLoginSuccessFul() =>
      DrawTogetherPrefs.saveLoginData(LoginData(userName.getText.toString, password.getText.toString))
      startActivity(SIntent[ChannelListActivity])

    case SLoginFailed(reason) =>
      password.setError(getString(R.string.login_failed) + s" $reason")
  }

}