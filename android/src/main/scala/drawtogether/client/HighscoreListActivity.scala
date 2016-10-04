package drawtogether.client

import android.view.View
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.ui.{FinishedGameRecyclingView, HeaderToolbar, LeftNavBar, UpdateButton}
import drawtogether.shared.communication.SharedMessages._

/**
  * This activity deals with incoming messages for the high score representation
  * The messages with data from server will be forwarded to the view
  */
class HighscoreListActivity extends DrawTogetherActivity with FinishedGameRecyclingView with HeaderToolbar with LeftNavBar with UpdateButton {

  /** xml layout name */
  val layoutId = R.layout.element_list_view_layout
  /** id on navigation bar */
  var navigationSelectionIndex = R.id.nav_highscore

  /** update the high score by sending a high score request to the server */
  def updateData() = sendMessage(CHighScoreDrawingsRequest())

  onCreate {
    // hide the floating button
    findView(TR.fab).setVisibility(View.GONE)
  }

  setOnReceive {
    // message from server with a drawing list sorted on score
    case SHighScoreDrawingsResponse(recentDrawings) => setFinishedDrawings(recentDrawings)
    // message from server with image in original size
    case SOriginalDrawingResponse(gameId, base64Image) => showOriginalImage(gameId, base64Image)
  }

}




