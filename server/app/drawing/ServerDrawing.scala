package drawing

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import drawtogether.shared.drawing.DrawingObjects.{DrawColor, DrawTarget}
import drawtogether.shared.settings.SharedSettings

/** This is the server side implementation of drawing to image  */
object ServerDrawing {

  /** image size */
  val imgSize = SharedSettings.IMAGE_SIZE

  /** converts the drawing to a bufferedImage that can be converted to jpg on the fly */
  def drawingToImage(drawImage: ServerDrawImage): BufferedImage = {
    // create bufferedImage
    val bufImage = new BufferedImage(drawImage.xSize, drawImage.ySize, BufferedImage.TYPE_INT_RGB)
    // fill it with our drawing
    import scalaxy.streams.optimize
    optimize {
      for (x <- 0 until drawImage.xSize;
           y <- 0 until drawImage.ySize) {
        //apply colours - keep x horizontal
        bufImage.setRGB(x, y, drawImage.getRawPixel(x, y))
      }
      // return
      bufImage
    }
  }

  /** converts a image to a JPG byteArray that can be send to the browser */
  def imageToJPGByteArray(bufferedImage: BufferedImage): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "jpg", outputStream)
    outputStream.toByteArray
  }

  /** converts a image to a JPG byteArray that can be send to the browser */
  def imageToPNGByteArray(bufferedImage: BufferedImage): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", outputStream)
    outputStream.toByteArray
  }

  /** resize an image to create a preview */
  def resizeImage(originalImage: BufferedImage, targetSize: Int = 150): BufferedImage = {
    val resizedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB)
    val g = resizedImage.createGraphics()
    g.drawImage(originalImage, 0, 0, targetSize, targetSize, null)
    g.dispose()
    resizedImage
  }


  /** a simple representation of a image with a mutable ImageBuffer */
  case class ServerDrawImage(image: Array[Int], xSize: Int = imgSize, ySize: Int = imgSize) extends DrawTarget {

    override def getRawPixel(x: Int, y: Int): Int = image((x % xSize) + y * xSize)

    override def setRawPixel(x: Int, y: Int, color: Int): Unit = image.update((x % xSize) + y * xSize, color)

  }

  object ServerDrawImage {
    /** creates an empty image */
    def createEmptyImage(): ServerDrawImage = {
      val imageArray = Array.fill[Int](imgSize * imgSize)(DrawColor.from(255, 255, 255))
      ServerDrawImage(imageArray, imgSize, imgSize)
    }
  }
}
