package drawtogether.client.ui

import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener
import drawtogether.client.TR
import drawtogether.client.drawing.ImagePosition
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.shared.communication.SharedMessages.CGameDrawCommand
import drawtogether.shared.drawing.Drawing2Image
import drawtogether.shared.drawing.DrawingObjects.DrawCommand
import drawtogether.shared.settings.SharedSettings

import scala.concurrent.Future

/** Defines an area where the user can draw on using touch events */
trait DrawingArea {
  self: DrawTogetherActivity with ColorPicker =>

  /** the imageView element to draw in */
  lazy val imageView = findView(TR.ingame_image)
  /** a seekBar for the pen size */
  lazy val seekBar = findView(TR.ingame_size_seek)

  onCreate {
    // on touch actions
    imageView.setOnTouchListener(new OnTouchListener {

      /** an option for a last touch position */
      var lastPositionOption: Option[(Int, Int)] = None

      override def onTouch(v: View, event: MotionEvent): Boolean = {
        // get touch position in imageCoord
        val (touchX, touchY) = ImagePosition.eventToImagePosition(imageView, event)

        // do not process negative or out of bounds values values and remove lastPosition
        if (touchX < 0 || touchX >= SharedSettings.IMAGE_SIZE || touchY < 0 || touchY >= SharedSettings.IMAGE_SIZE) {
          lastPositionOption = None
          return false
        }

        // react to the different touch events
        event.getAction match {

          case MotionEvent.ACTION_DOWN =>
            lastPositionOption = Some((touchX, touchY))
            val command = DrawCommand(touchX, touchY, touchX, touchY, useBresenham = false, seekBar.getProgress + 1, colorPickerSelection)
            sendMessage(CGameDrawCommand(command))
            applyDrawingCommand(command)

          case MotionEvent.ACTION_UP =>
            // reset lastPosition on touch stop
            lastPositionOption = None

          case MotionEvent.ACTION_MOVE =>
            // if position is available
            lastPositionOption.foreach(lastPos => {
              // start task on other thread
              // get last distance position
              val (lastX, lastY) = lastPos
              // distance should be higher than 0.3f to reduce the calculation
              if ((lastX - touchX) * (lastX - touchX) + (lastY - touchY) * (lastY - touchY) > 0.5f) {
                // create a command using the old and new position and activating bresenham for path interpolation
                val command = DrawCommand(lastX, lastY, touchX, touchY, useBresenham = true, seekBar.getProgress + 1, colorPickerSelection)
                // send command
                sendMessage(CGameDrawCommand(command))
                // apply the actionList to the drawTarget
                applyDrawingCommand(command)
              }
            })
            // set current position as last
            lastPositionOption = Some(touchX, touchY)

          case _ => // other not relevant touch events
        }
        true // true as we have consumed the event
      }
    })
  }

  /** each drawing command will be applayed on the local data object and represented on the gui */
  def applyDrawingCommand(command: DrawCommand): Unit = {
    // as it requires some cpu power, we use a future
    Future {
      data(d => {
        Drawing2Image.applyToDrawing(d.drawTarget, command)
        runOnUiThread(imageView.setImageBitmap(d.drawTarget.appliedBitmap()))
      })
    }
  }
}
