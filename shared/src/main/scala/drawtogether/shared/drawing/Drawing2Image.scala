package drawtogether.shared.drawing

import drawtogether.shared.drawing.DrawingObjects.{DrawCommand, DrawTarget}

/** This code creates an image out of drawingCommands */
object Drawing2Image {

  /** takes a drawTarget and a command and renders all commands to the drawTarget */
  def applyToDrawing(drawTarget: DrawTarget, c: DrawCommand): DrawTarget = {

    c.useBresenham match {
      // this is a single point, no interpolation
      case false => softDraw(drawTarget, c.x0, c.y0, c.size, c.color)

      // this is a path, apply bresenham
      case true =>
        val iterator = BitmapOps.bresenham(c.x0, c.y0, c.x1, c.y1)
        // for huge sizes the amount of processing a softDraw is very huge
        // so we throw away some steps of the bresenham results
        // bigger sizes result in higher throw away rates
        val dropRatio = math.max(c.size / 5, 1)
        iterator.grouped(dropRatio).map(_.head).foreach(x => softDraw(drawTarget, x._1, x._2, c.size, c.color))
    }
    drawTarget
  }

  def softDraw(drawTarget: DrawTarget, x: Int, y: Int, size: Int, color: Int): Unit = {
    // apply stencil
    // ! this code is hot, so we use scalaxy to optimize the scala code
    import scalaxy.streams.optimize
    optimize {
      val halfSize = size / 2
      for (xOffset <- -halfSize to halfSize;
           yOffset <- -halfSize to halfSize;
           intensity = 1 - ((xOffset * xOffset) + (yOffset * yOffset)).toFloat / (halfSize * halfSize)
           if intensity > 0) {
        drawTarget.setPixel(x + xOffset, y + yOffset, color, math.min(1, 3 * intensity))
      }
    }
  }

}
