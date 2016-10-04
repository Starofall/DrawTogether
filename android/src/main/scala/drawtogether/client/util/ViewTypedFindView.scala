package drawtogether.client.util

import android.view.View
import drawtogether.client.TypedFindView

/** allows the usage of the TypedFindView on views instead of the global context */
trait ViewTypedFindView extends TypedFindView {

  /** view that should be searched */
  val view: View

  /** forward the event to the view */
  override protected def findViewById(id: Int): View = view.findViewById(id)
}
