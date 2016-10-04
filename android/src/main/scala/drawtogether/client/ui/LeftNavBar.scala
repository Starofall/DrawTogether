package drawtogether.client.ui

import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import drawtogether.client._
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.DrawTogetherPrefs
import org.scaloid.common.SIntent

/**
  * The navigation bar contains the following menu items:
  * ChannelList
  * MyDrawings
  * Recent Drawings
  * High Score
  * Logout
  * (see: activity_main_drawer.xml)
  * Each item leads to the next activity
  */
trait LeftNavBar {
  /* the trait is able to use the fields or methods of a class DrawTogetherActivity
   (mix in)and HeaderToolbar,this is done by specifying a self type for the trait */
  self: DrawTogetherActivity with HeaderToolbar =>

  /** the navigation area view */
  lazy val navigationView = findView(TR.nav_view)
  /** the drawer view */
  lazy val drawer = findView(TR.drawer_layout)

  /** abstract val for the navigation selection index */
  var navigationSelectionIndex: Int

  onCreate {
    //navigation events: each item leads to the predefined activity
    navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener {
      override def onNavigationItemSelected(menuItem: MenuItem): Boolean = {
        menuItem.getItemId match {
          case R.id.nav_channel_list    => startActivity(SIntent[ChannelListActivity]);
          case R.id.nav_user_drawings   => startActivity(SIntent[UserDrawingsActivity])
          case R.id.nav_recent_drawings => startActivity(SIntent[RecentListActivity])
          case R.id.nav_highscore       => startActivity(SIntent[HighscoreListActivity])
          case R.id.nav_logout          => DrawTogetherPrefs.cleanPreferences(); startActivity(SIntent[LoginActivity])
          case _                        => Log.e("NAV", getString(R.string.not_implemented_exception))
        }
        drawer.closeDrawer(GravityCompat.START)
        true
      }
    })

    // set the index from the abstract navigationSelectionIndex as checked
    navigationView.getMenu.findItem(navigationSelectionIndex).setChecked(true)

    // sync drawer and navigation
    val toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.abc_capital_on, R.string.abc_capital_off)
    drawer.addDrawerListener(toggle)
    toggle.syncState()

    // set the user name from login data on navigation bar with "Hello" at the beginning
    val userNameNav = navigationView.getHeaderView(0).findViewById(R.id.user_name_nav_bar).asInstanceOf[TextView]
    DrawTogetherPrefs.loadSavedLoginData().foreach { l =>
      userNameNav.setText(s"Hello ${l.username.capitalize}!")
    }
  }

}
