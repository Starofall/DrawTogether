package drawtogether.client

import android.animation.ObjectAnimator
import android.graphics.{BitmapFactory, Color}
import android.util.{Base64, Log}
import android.view.animation.LinearInterpolator
import android.widget.TextView
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.ui.{ColorPicker, DrawingArea}
import drawtogether.shared.communication.SharedMessages._
import drawtogether.shared.ingame.InGame._
import org.scaloid.common._

/**
  * This activity deals with incoming messages for the game itself
  * The messages with data from server will be stored into the local variables
  * and represented on the view
  */
class InGameActivity extends DrawTogetherActivity with ColorPicker with DrawingArea {

  /** maximal length of a player name */
  final val maxPlayerNameLength = 8

  /** xml layout name */
  val layoutId: Int = R.layout.ingame_layout
  /** seconds timer to show the actual seconds */
  lazy val secondsTimer = findView(TR.ingame_timer)
  /** button to activate the color picker */
  lazy val chooseColorBtn = findView(TR.ingame_color_btn)
  /** the inner layout in game */
  lazy val layout = findView(TR.ingame_main_layout)
  /** round counter */
  lazy val roundNumberText = findView(TR.ingame_round_number)
  /** the actual game name */
  lazy val gameName = findViewById(R.id.game_title_ingame).asInstanceOf[TextView]

  // 4 player names fields
  lazy val player1Field = findView(TR.ingame_player_1)
  lazy val player2Field = findView(TR.ingame_player_2)
  lazy val player3Field = findView(TR.ingame_player_3)
  lazy val player4Field = findView(TR.ingame_player_4)

  //dummy settings
  var userName = ""
  var gameSettings = GameSettings("dummy", 1, 1)

  onResume {
    // default is disabled
    imageView.setEnabled(false)

    // gets the name from data object
    userName = loadUsername()
    secondsTimer.setProgress(0)
    data(d => d.currentGame match {
      case Some(runningGame) =>
        gameSettings = runningGame.settings

        runningGame.base64OriginalImage.foreach(i => {
          val buffer = Base64.decode(i, Base64.DEFAULT)
          val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length)
          d.drawTarget.loadFromBitmap(bitmap)
        })
        gameName.setText(gameSettings.title)
      case None              =>
        Log.e("Error", "No settings available")
    })
  }

  onCreate {
    //get the actual picture
    data(d => imageView.setImageBitmap(d.drawTarget.appliedBitmap()))
    chooseColorBtn.onClick(showColorPicker())
    chooseColorBtn.setBackgroundColor(colorPickerSelection)
  }

  setOnReceive {

    //each draw command will be represented on the view
    case SGameDrawCommand(command) =>
      applyDrawingCommand(command)

    //actual users in the game
    case SGameUserList(users) =>
      setPlayerList(users)

    //actual state of the game
    case SGameState(state, roundNumber) =>
      roundNumberText.setText((gameSettings.totalRounds - roundNumber).toString)
      state match {
        //not enough users->disable the image
        case WaitingState =>
          imageView.setEnabled(false)
          imageView.setColorFilter(Color.argb(126, 255, 255, 255))

        // the game is over!
        case DoneState =>
          startActivity(SIntent[GameOverActivity])

        //which user is drawing now and how much seconds he has
        case UserDrawingState(user, remainingSeconds) =>
          setTimerAnimated(remainingSeconds)
          // if the current use is drawing
          if (user.name.equalsIgnoreCase(userName)) {
            imageView.setColorFilter(Color.argb(0, 255, 255, 255))
            imageView.setEnabled(true)
          } else {
            //if another player is drawing
            imageView.setColorFilter(Color.argb(145, 255, 255, 255))
            imageView.setEnabled(false)
          } //enable the player names depend on who is drawing now
          Seq(player1Field, player2Field, player3Field, player4Field).foreach { f =>
            // active if name == currentUser
            f.setEnabled(f.getText.toString.equalsIgnoreCase(user.name.take(maxPlayerNameLength)))
          }
      }
  }

  /** if the back option chosen, the user can leave the game the notification about leaving */
  override def onBackPressed(): Unit = {
    new AlertDialogBuilder("Exit the game", "Do you really want to this game?") {
      positiveButton("Exit", exitGame())
      negativeButton(android.R.string.cancel)
    }.show()
  }

  /** leave game and do to the channel list */
  def exitGame() = {
    sendMessage(CGameLeaveRequest())
    startActivity(SIntent[ChannelListActivity])
  }

  /** animates the counter */
  var timerAnimation: Option[ObjectAnimator] = None

  /** animated timer */
  def setTimerAnimated(remainingSeconds: Int): Unit = {
    if (remainingSeconds == gameSettings.secondsPerRound) {
      timerAnimation.foreach(_.cancel())
      secondsTimer.setProgress(0)
    } else {
      val newValue = (100 * (gameSettings.secondsPerRound - remainingSeconds)) / gameSettings.secondsPerRound
      timerAnimation.foreach(_.cancel())
      timerAnimation = Some(ObjectAnimator.ofInt(secondsTimer, "progress", newValue))
      timerAnimation.foreach { animation =>
        animation.setDuration(1000)
        animation.setInterpolator(new LinearInterpolator())
        animation.start()
      }
    }
  }

  /** set names to the player */
  def setPlayerList(users: List[User]): Unit = {
    error("" + users)
    Seq((player1Field, 0), (player2Field, 1), (player3Field, 2), (player4Field, 3)).foreach { t =>
      t._1.setText(users.lift(t._2).map(_.name.take(maxPlayerNameLength)).getOrElse("Empty"))
    }
  }

  /** update the color */
  override def colorGotUpdated(oldColor: Int, newColor: Int): Unit = {
    chooseColorBtn.setBackgroundColor(newColor)
  }
}