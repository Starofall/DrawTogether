package drawtogether.client


import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.services.LocalDataService.RunningGame
import drawtogether.client.ui.{HeaderToolbar, InGameRecyclingView, LeftNavBar, UpdateButton}
import drawtogether.shared.communication.SharedMessages.{SGameState, _}
import org.scaloid.common._

/**
  * This activity deals with incoming messages for all running games
  * The messages with data from server will be stored into the local variables
  * or/and forwarded to the view
  */
class ChannelListActivity extends DrawTogetherActivity with InGameRecyclingView with HeaderToolbar with LeftNavBar with UpdateButton {

  /** id of xml layout file */
  val layoutId: Int = R.layout.element_list_view_layout
  /** channel list activity id on navigation bar */
  var navigationSelectionIndex: Int = R.id.nav_channel_list

  /** update data on change */
  def updateData(): Unit = sendMessage(CRunningGameListRequest())


  onCreate {
    //on click go to the create game activity
    findView(TR.fab).onClick(startActivity(SIntent[CreateGameActivity]))
  }

  setOnReceive {
    /* incoming messages from server with the list of all running games
    will be forwarded to InGameRecyclingView */
    case SRunningGameListResponse(games) => setRunningGames(games)

    // on join the user gets the actual image
    case SJoinGameSuccessful(id, settings, base64pngImage) => data(d => {
      d.currentGame = Some(RunningGame(id, settings, base64pngImage))
      d.drawTarget.clean()
      startActivity(SIntent[InGameActivity])
    })

    case SJoinGameRejected(reason) => toast("Join rejected:" + reason)

    // postpone these message in case of join
    case n: SGameUserList => postponeForNextActivity(n)
    case n: SGameState    => postponeForNextActivity(n)
  }

}




