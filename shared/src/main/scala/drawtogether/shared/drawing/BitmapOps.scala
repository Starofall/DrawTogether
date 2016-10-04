package drawtogether.shared.drawing

/** this object contains the bresenham function */
object BitmapOps {

  /**
    * Bresenham algorithm for drawing lines
    * Code/Idea: https://raw.githubusercontent.com/acmeism/RosettaCodeData/master/Task/Bitmap-Bresenhams-line-algorithm/Scala/bitmap-bresenhams-line-algorithm.scala
    */
  def bresenham(x0: Int, y0: Int, x1: Int, y1: Int): Iterator[(Int, Int)] = {
    val dx = math.abs(x1 - x0)
    val sx = if (x0 < x1) 1 else -1
    val dy = math.abs(y1 - y0)
    val sy = if (y0 < y1) 1 else -1
    def it = new Iterator[(Int, Int)] {
      var x = x0
      var y = y0
      var err = (if (dx > dy) dx else -dy) / 2

      def next = {
        val res = (x, y)
        val e2 = err
        if (e2 > -dx) {
          err -= dy
          x += sx
        }
        if (e2 < dy) {
          err += dx
          y += sy
        }
        res
      }

      def hasNext = sx * x <= sx * x1 && sy * y <= sy * y1
    }
    it
  }
}
