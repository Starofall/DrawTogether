package drawtogether.shared.drawing

import drawtogether.shared.settings.SharedSettings

/** Contains objects for drawing */
object DrawingObjects {

  /** a wrapper object containing colors */
  object DrawColor {
    def r(rgb: Int) = (rgb >> 16) & 0xFF

    def g(rgb: Int) = (rgb >> 8) & 0xFF

    def b(rgb: Int) = (rgb >> 0) & 0xFF

    /** set of predefined colors for testing */
    val GREEN = from(0, 255, 0)
    val RED = from(255, 0, 0)

    /** helper to create DrawColor from RGB values */
    def from(r: Int, g: Int, b: Int): Int = (0xFF << 24) | (r << 16) | (g << 8) | b
  }


  /**
    * this event is created when the users draws something on the client
    * x0/y0 are the start positions
    * x1/y1 are the target positions
    * useBresenham is true if we need to interpolate between the positions
    */
  case class DrawCommand(x0: Int, y0: Int, x1: Int, y1: Int, useBresenham: Boolean, size: Int = 1, color: Int)

  /** a cross platform implementation of a drawTarget */
  abstract class DrawTarget {

    /** image size */
    final val imgSize = SharedSettings.IMAGE_SIZE

    // platform specific actions according to the environment implementation

    protected def getRawPixel(x: Int, y: Int): Int

    protected def setRawPixel(x: Int, y: Int, newColor: Int)


    /** set the colour at x,y to drawColor (optional with intensity) - error save to outOfBounds */
    def setPixel(x: Int, y: Int, newColor: Int, intensity: Float = 1): Any = {
      val pixel = getPixel(x, y)
      // apply color based on intensity
      val applyColor = DrawColor.from(
        (DrawColor.r(pixel) * (1 - intensity) + DrawColor.r(newColor) * intensity).toInt,
        (DrawColor.g(pixel) * (1 - intensity) + DrawColor.g(newColor) * intensity).toInt,
        (DrawColor.b(pixel) * (1 - intensity) + DrawColor.b(newColor) * intensity).toInt)
      // update the pixel
      setPixel(x, y, applyColor)
    }

    /** we forward the command to the platform specific commands - also prevents outOfBounds */
    def getPixel(x: Int, y: Int): Int = {
      if (y < 0 || x < 0 || x >= imgSize || y >= imgSize) {
        0 // no option -> because of optimization for performance
      } else {
        getRawPixel(x, y)
      }
    }

    /** we forward the command to the platform specific commands - also prevents outOfBounds */
    def setPixel(x: Int, y: Int, newColor: Int) = {
      if (y < 0 || x < 0 || x >= imgSize || y >= imgSize) {
        // nothing
      } else {
        setRawPixel(x, y, newColor)
      }

    }
  }
}
