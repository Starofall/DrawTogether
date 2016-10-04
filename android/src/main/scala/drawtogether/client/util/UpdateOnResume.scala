package drawtogether.client.util

import org.scaloid.common.SActivity

/** a trait for a activity that gets its data updated on resume using the abstract updateData function */
trait UpdateOnResume {
  self: SActivity =>

  // call the update function onResume
  onResume(updateData())

  /** function that is called on resume */
  def updateData(): Unit
}
