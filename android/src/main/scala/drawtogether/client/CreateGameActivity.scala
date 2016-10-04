package drawtogether.client

import android.support.design.widget.Snackbar
import android.view.View
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, Spinner, ArrayAdapter}
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.services.LocalDataService.RunningGame
import drawtogether.client.ui.HeaderToolbar
import drawtogether.shared.communication.SharedMessages._
import drawtogether.shared.ingame.InGame.GameSettings
import org.scaloid.common._

/**
  * This activity takes care about creation of the new game
  * Contains Title,Rounds and Time settings for a single game
  */
class CreateGameActivity extends DrawTogetherActivity with HeaderToolbar {

  /** id of xml layout file */
  val layoutId: Int = R.layout.game_settings_layout
  /** create game button */
  lazy val start_game = findView(TR.btn_goto_game)
  /** title of the game */
  lazy val group_name = findView(TR.group_name_topic)
  /** table layout with settings */
  lazy val create_game_settings = findView(TR.create_game_settings)
  /** spinner element for rounds */
  lazy val spinnerWithRounds = findView(TR.user_rounds)
  /** spinner element for the seconds */
  lazy val spinnerWithSeconds = findView(TR.user_seconds)
  /** round counter */
  var rounds = ""
  /** text with count of rounds */
  var seconds = ""


  onCreate {
    // Set back button
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    getSupportActionBar.setDisplayShowHomeEnabled(true)
    // set spinner to the array with rounds
    createSpinnerMenu(spinnerWithRounds, R.array.rounds, "rounds")
    // set spinner to the array with seconds
    createSpinnerMenu(spinnerWithSeconds, R.array.seconds, "seconds")
    start_game.onClick(group_name.text.toString match {
      case ""   => group_name.setError("Please define the group name!")
      case name => sendMessage(CCreateGameRequest(GameSettings(name, rounds.toInt, seconds.toInt)))
    })
  }

  setOnReceive {
    // if join successful go to the next activity
    case SJoinGameSuccessful(id, settings, base64pngImage) => data(d => {
      d.currentGame = Some(RunningGame(id, settings, base64pngImage))
      d.drawTarget.clean()
      runOnUiThread(startActivity(SIntent[InGameActivity]))
    })
    // we have not yet changed the activity, but need to know the users, so store them
    case n: SGameUserList => postponeForNextActivity(n)
    case n: SGameState    => postponeForNextActivity(n)
  }


  /** *
    * Set adapter to the spinner
    *
    * @param spinner       the spinner ui element
    * @param inputIdLayout :Array Id in @string/
    */
  def createSpinnerMenu(spinner: Spinner, inputIdLayout: Int, changeValue: String): Unit = {
    // Create an ArrayAdapter using the string array and a default spinner layout
    val adapter = ArrayAdapter.createFromResource(this, inputIdLayout, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter)
    spinner.setOnItemSelectedListener(new OnItemSelectedListener {
      override def onNothingSelected(parent: AdapterView[_]): Unit = runOnUiThread(Snackbar.make(create_game_settings, s"Please define the rounds!", Snackbar.LENGTH_LONG).show())

      override def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
        val textToChange = parent.getItemAtPosition(position).toString
        changeValue match {
          case "rounds"  => rounds = textToChange
          case "seconds" => seconds = textToChange
        }
      }
    })
  }

}


