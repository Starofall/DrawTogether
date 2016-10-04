package drawtogether.client.ui

import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.{Menu, MenuItem}
import drawtogether.client.R
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.UpdateOnResume

/** adds an update button to the header toolbar */
trait UpdateButton extends UpdateOnResume {
  /* the trait is able to use the fields or methods of a class DrawTogetherActivity, HeaderToolbar and LeftNavBar
  this is done by specifying a self type for the trait */
  self: DrawTogetherActivity with HeaderToolbar with LeftNavBar =>


  onCreate {
    // on click the data will get updated
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      def onMenuItemClick(menuItem: MenuItem): Boolean = {
        menuItem.getItemId match {
          case R.id.action_refresh =>
            // call the function from the UpdateOnResume trait
            updateData()
            Snackbar.make(self.drawer, "Updated Data!", Snackbar.LENGTH_LONG).show()
            true
          case _                   => false
        }
      }
    })
  }

  /** set include_toolbar update button */
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.toolbar_update_menu, menu)
    true
  }
}
