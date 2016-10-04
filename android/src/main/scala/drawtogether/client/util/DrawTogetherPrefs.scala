package drawtogether.client.util

/** wrapper around the android preference system */
object DrawTogetherPrefs {

  /** Try to get the login data we used before */
  def loadSavedLoginData()(implicit base: android.content.Context): Option[LoginData] = {
    val preferences = base.getSharedPreferences(DrawTogetherPrefs.PREF_FILE_NAME, 0)
    val usr = preferences.getString("username", "")
    val pwd = preferences.getString("password", "")
    if (usr != "" && pwd != "") {
      Some(LoginData(usr, pwd))
    } else {
      None
    }
  }

  /** Saves user credential persistent as string */
  def saveLoginData(loginData: LoginData)(implicit base: android.content.Context) {
    val preferences = base.getSharedPreferences(DrawTogetherPrefs.PREF_FILE_NAME, 0)
    val editor = preferences.edit()
    editor.putString("username", loginData.username)
    editor.putString("password", loginData.password)
    editor.commit()
  }

  /** delete preferences */
  def cleanPreferences()(implicit base: android.content.Context): Unit = {
    val preferences = base.getSharedPreferences(DrawTogetherPrefs.PREF_FILE_NAME, 0)
    preferences.edit.clear.commit()
  }

  /** the filename the data is stored */
  final val PREF_FILE_NAME = "drawtogether.client.pref_file"
}

