package drawtogether.client

import drawtogether.client.services._
import drawtogether.client.util.{DrawTogetherPrefs, LoginData}
import drawtogether.shared.communication.SharedMessages.{CRegisterRequest, SRegisterFailed, SRegisterSuccessFul}
import org.scaloid.common._

/** This activity deals with incoming messages for the registration on server */
class RegisterActivity extends DrawTogetherActivity {

  /** xml layout name */
  val layoutId = R.layout.signup_layout

  /** text area for userName */
  lazy val userNameTxt = findView(TR.userNameSignUp)
  /** text area for password */
  lazy val passwordTxt = findView(TR.signUp_password)
  /** text area for retryPassword */
  lazy val retryPasswordTxt = findView(TR.signUp_password_retry)
  /** button for signup */
  lazy val signUpBtn = findView(TR.btn_signUp)

  onCreate {
    getWindow.setBackgroundDrawableResource(R.drawable.background)

    signUpBtn.onClick {
      (userNameTxt.getText.toString, passwordTxt.getText.toString, retryPasswordTxt.getText.toString) match {
        case ("", _, _)                => userNameTxt.setError("Please enter a username!")
        case (a, _, _) if a.length < 4 => userNameTxt.setError("Username to short!")
        case (_, "", _)                => passwordTxt.setError("Please enter a password!")
        case (_, a, b) if a != b       => retryPasswordTxt.setError("Does not match password!")
        case (a, b, _)                 => sendMessage(CRegisterRequest(a, b))
      }
    }
  }

  setOnReceive {
    case SRegisterFailed(reason) =>
      toast("Registration error:" + reason)
      userNameTxt.setError(reason)

    case SRegisterSuccessFul(userName) =>
      toast(userName + " was registered ")
      DrawTogetherPrefs.saveLoginData(LoginData(userNameTxt.text.toString, passwordTxt.text.toString))
      startActivity(SIntent[LoginActivity])
  }

}
