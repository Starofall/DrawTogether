package drawtogether.client.ui

import android.content.DialogInterface
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.{ColorPickerClickListener, ColorPickerDialogBuilder}
import drawtogether.shared.drawing.DrawingObjects.DrawColor
import org.scaloid.common.SActivity

/** a trait that can be mixed in to enable the use aof a colorPicker through the variable holding the last selected color */
trait ColorPicker {
  self: SActivity =>

  /** default color of the color picker */
  var colorPickerSelection = DrawColor.GREEN

  /** callback for the activity on color change */
  def colorGotUpdated(oldColor: Int, newColor: Int): Unit = {}

  /** an instance of the color picker that can be shown to the user */
  lazy val colorPickerDialog = {
    ColorPickerDialogBuilder
      .`with`(this)
      .setTitle("Choose color")
      .initialColor(colorPickerSelection)
      .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
      .density(10)
      .lightnessSliderOnly()
      .setPositiveButton("ok", new ColorPickerClickListener() {
        def onClick(dialogInterface: DialogInterface, i: Int, integers: Array[Integer]): Unit = {
          // call callback
          colorGotUpdated(colorPickerSelection, i)
          colorPickerSelection = i
        }
      })
      .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {}
      })
      .build()
  }

  /** shows the color picker as a dialog to the suer */
  def showColorPicker() = {
    colorPickerDialog.show()
  }

}
