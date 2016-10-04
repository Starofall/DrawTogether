package drawtogether.client.drawing

import android.graphics.Matrix
import android.view.MotionEvent
import android.widget.ImageView

/** A small util class for image touch positioning */
object ImagePosition {

  // saves object creations - not thread save, but there should only be one UI thread
  val inverse = new Matrix()
  val touchPoint = Array[Float](0, 0)

  /** returns the pixel position for a given imageView and motionEvent */
  def eventToImagePosition(imageView: ImageView, event: MotionEvent): (Int, Int) = {
    imageView.getImageMatrix.invert(inverse)
    // map touch point from ImageView to image
    touchPoint(0) = event.getX()
    touchPoint(1) = event.getY()
    inverse.mapPoints(touchPoint)
    (touchPoint(0).toInt, touchPoint(1).toInt)
  }

}
