package drawtogether.client

import android.view.View
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.ui.{FinishedGameRecyclingView, HeaderToolbar, LeftNavBar, UpdateButton}
import drawtogether.shared.communication.SharedMessages._

/** All previously finished games activity */
class RecentListActivity extends DrawTogetherActivity with FinishedGameRecyclingView with HeaderToolbar with LeftNavBar with UpdateButton {

  /** id of xml layout file */
  val layoutId = R.layout.element_list_view_layout
  /** id on the navigation bar */
  var navigationSelectionIndex = R.id.nav_recent_drawings

  /** sends an update request to the server */
  def updateData() = sendMessage(CRecentDrawingsRequest())

  onCreate {
    // hide the floating action button
    findView(TR.fab).setVisibility(View.GONE)
  }

  setOnReceive {
    /* incoming messages from server with the list of all finished games
    will be forwarded to FinishedGameRecyclingView */
    case SRecentDrawingsResponse(finishedDrawings) => setFinishedDrawings(finishedDrawings)
    /* message from server with image in original size*/
    case SOriginalDrawingResponse(gameId, base64Image) => showOriginalImage(gameId, base64Image)
  }

}