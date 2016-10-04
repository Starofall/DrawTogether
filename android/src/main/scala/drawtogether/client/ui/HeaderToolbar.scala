package drawtogether.client.ui

import drawtogether.client.TR
import drawtogether.client.services.DrawTogetherActivity

/** a trait that implements a header toolbar into the activity */
trait HeaderToolbar {
  self: DrawTogetherActivity =>

  /** the toolbar used in the activity */
  lazy val toolbar = findView(TR.toolbar)

  onCreate {
    // set the toolbar into the activity
    setSupportActionBar(toolbar)
  }
}
