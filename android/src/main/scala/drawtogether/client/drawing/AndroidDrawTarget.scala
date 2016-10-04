package drawtogether.client.drawing

import android.graphics.{Bitmap, Color}
import drawtogether.shared.drawing.DrawingObjects.DrawTarget

/** the android implementation of a drawTarget working with Bitmaps */
case class AndroidDrawTarget() extends DrawTarget {

  /** an int array containing all image colors - used as it is faster than bitmap access */
  var imageArray = Array.fill[Int](imgSize * imgSize)(Color.WHITE)

  /** creates the image on the android memory */
  val bitmap = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888)

  /** true if the image got changed, will trigger a setPixels on the bitmap */
  var changed = false

  /** as we work on array data, we here need to push the array to the bitmap */
  def appliedBitmap(): Bitmap = {
    if (changed) {
      changed = false
      bitmap.setPixels(imageArray, 0, imgSize, 0, 0, imgSize, imgSize)
      bitmap
    } else {
      bitmap
    }
  }

  /** returns the value of (x,y) as intColor */
  override def getRawPixel(x: Int, y: Int): Int = imageArray((x % imgSize) + y * imgSize)

  /** sets the pixel at (x,y) with newColor */
  override def setRawPixel(x: Int, y: Int, newColor: Int): Unit = {
    imageArray.update((x % imgSize) + y * imgSize, newColor)
    changed = true
  }

  /** clean the image an fill it with white */
  def clean(): Unit = {
    imageArray = Array.fill[Int](imgSize * imgSize)(Color.WHITE)
    changed = true
  }

  /** loads an image from the bitmap into the imageArray */
  def loadFromBitmap(bm: Bitmap): Unit = bm.getPixels(imageArray, 0, imgSize, 0, 0, imgSize, imgSize)
}