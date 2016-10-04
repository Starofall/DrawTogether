package drawtogether.client

import android.view.View
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.ui.{FinishedGameRecyclingView, HeaderToolbar, LeftNavBar, UpdateButton}
import drawtogether.shared.communication.SharedMessages.{SOriginalDrawingResponse, CUserDrawingsRequest, SUserDrawingsResponse}

/** Shows a list of all drawings a user made */
class UserDrawingsActivity extends DrawTogetherActivity with FinishedGameRecyclingView with HeaderToolbar with LeftNavBar with UpdateButton {

  /** xml layout name */
  val layoutId = R.layout.element_list_view_layout
  // id on navigation bar
  var navigationSelectionIndex = R.id.nav_user_drawings

  /** update the user drawings by sending a user drawings request to the server */
  def updateData() = sendMessage(CUserDrawingsRequest())

  onCreate {
    // hide the floating action button
    findView(TR.fab).setVisibility(View.GONE)
  }

  setOnReceive {
    // message from server with a drawing list
    case SUserDrawingsResponse(finishedDrawings) => setFinishedDrawings(finishedDrawings)
    // message from server with image in original size
    case SOriginalDrawingResponse(gameId, base64Image) => showOriginalImage(gameId, base64Image)
  }

}
