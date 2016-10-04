package drawtogether.client

import android.util.Log
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.shared.communication.SharedMessages.SGameUserList
import drawtogether.shared.ingame.InGame.GameSettings
import org.scaloid.common._

/**
  * The game over activity.
  * Shows the finished image, title of the game, player count and rounds
  */
class GameOverActivity extends DrawTogetherActivity {

  /** xml file id */
  val layoutId = R.layout.game_over_layout
  /** finished picture */
  lazy val image = findView(TR.game_over_picture)
  /** exit button */
  lazy val button = findView(TR.btn_game_over)
  /** dummy settings */
  var gameSettings = GameSettings("dummy", 1, 1)
  /** player count text view */
  lazy val playerCount = findView(TR.player_count_over_game)
  /** round count text view */
  lazy val roundsCount = findView(TR.rounds_count_over_game)
  /** game title text view */
  lazy val gameName = findView(TR.game_name_over_game)

  onCreate {
    showGameOver()
    //shows on success game settings: rounds count, game title
    data(d => d.currentGame match {
      case Some(runningGame) =>
        gameSettings = runningGame.settings
        roundsCount.setText(gameSettings.totalRounds.toString)
        gameName.setText(gameSettings.title.toString)
      case None              =>
        Log.e("Error", "No settings available")
    })
    button.onClick(startActivity(SIntent[UserDrawingsActivity]))
  }

  setOnReceive {
    /** incoming message with user list for the actual game */
    case SGameUserList(users) =>
      playerCount.setText(users.size.toString)
  }

  /** *
    * shows game over layout with finished image
    */
  def showGameOver(): Unit = {
    data(d => {
      runOnUiThread({
        image.setImageBitmap(d.drawTarget.appliedBitmap())
      })
    })
  }
}