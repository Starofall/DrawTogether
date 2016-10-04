package drawtogether.client.ui

import android.graphics.BitmapFactory
import android.support.v7.widget.{CardView, LinearLayoutManager, RecyclerView}
import android.util.Base64
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.ViewTypedFindView
import drawtogether.client.{R, TR}
import drawtogether.shared.communication.SharedMessages.{COriginalDrawingRequest, CStaredRequest}
import drawtogether.shared.ingame.InGame.FinishedDrawing
import org.scaloid.common._

/** GUI trait for all finished games: High Score, Recent, User Drawings */
trait FinishedGameRecyclingView {
  /* the trait is able to use the fields or methods of a class DrawTogetherActivity (mix in),
   this is done by specifying a self type for the trait.*/
  self: DrawTogetherActivity =>

  /** local copy of the list of finished drawings */
  private var finishedGames = List[FinishedDrawing]()
  /** adapter for the view */
  private val adapter = new RVAdapterFinished()

  /** set finished drawings to local list finishedGames and notify the adapter about changes */
  def setFinishedDrawings(finishedDrawings: List[FinishedDrawing]): Unit = {
    finishedGames = finishedDrawings
    adapter.notifyDataSetChanged()
  }

  /**
    * Shows the original image in alert dialog
    *
    * @param gameId      the actual game id
    * @param base64Image image in string with base64
    */
  def showOriginalImage(gameId: String, base64Image: String): Unit = {
    // extract image
    val buffer = Base64.decode(base64Image, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length)
    // create view
    val inflatedView = getLayoutInflater.inflate(R.layout.original_image_layout, null)
    val originalImage = inflatedView.findViewById(R.id.original_image).asInstanceOf[ImageView]
    // set image
    originalImage.setImageBitmap(bitmap)
    // build dialog
    val alertBuilder = new AlertDialogBuilder("Picture", "")
    alertBuilder.negativeButton(android.R.string.ok)
    val dialog = alertBuilder.create()
    dialog.setView(inflatedView)
    dialog.show()
  }

  onCreate {
    val recList = findView(TR.channelListRect)
    recList.setHasFixedSize(true)
    val llm = new LinearLayoutManager(this)
    recList.setLayoutManager(llm)
    recList.setAdapter(adapter)
  }

  /**
    * Adapter manages the card view representation for all finished games
    * If the data will get updated the adapter changes the values on gui as well
    */
  class RVAdapterFinished extends RecyclerView.Adapter[FinishedGameHolder] {

    /** returns the number of finishedGames */
    override def getItemCount: Int = finishedGames.size

    /** fills the gui elements with data */
    override def onBindViewHolder(vh: FinishedGameHolder, i: Int): Unit = {
      val currentGame = finishedGames(i)
      vh.gameTitle.setText(currentGame.gameSettings.title)
      vh.totalRounds.setText(currentGame.gameSettings.totalRounds.toString)
      vh.starsCount.setText(currentGame.stars.toString)
      vh.seconds.setText(currentGame.gameSettings.secondsPerRound.toString)
      val buffer = Base64.decode(currentGame.base64Image, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length)
      vh.finishedImage.setImageBitmap(bitmap)
      vh.finishedImage.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = {
          sendMessage(COriginalDrawingRequest(currentGame.gameId))
        }
      })

      // apply user stared to view
      currentGame.userStared match {
        case true  =>
          vh.starBtn.setSelected(true)
          vh.starBtn.setImageResource(R.drawable.ic_star_on)
        case false =>
          vh.starBtn.setSelected(false)
          vh.starBtn.setImageResource(R.drawable.ic_star_off)
      }
      // add click listener
      val adapter = this // for callback usage
      vh.starBtn.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = {
          currentGame.userStared match {
            case true  =>
              currentGame.stars -= 1
              currentGame.userStared = false
            case false =>
              currentGame.stars += 1
              currentGame.userStared = true
          }

          /** data was changed */
          adapter.notifyDataSetChanged()

          /** send message with user input */
          sendMessage(CStaredRequest(finishedGames(i).userStared, finishedGames(i).gameId))
        } // end of onclick
      })

    }

    /** create a view holder for a finishedGame */
    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): FinishedGameHolder = {
      new FinishedGameHolder(
        LayoutInflater.from(viewGroup.getContext).inflate(R.layout.card_layout_finished, viewGroup, false)
      )
    }
  }

  /** view group to represent the info about the game. */
  class FinishedGameHolder(val view: View) extends RecyclerView.ViewHolder(view) with ViewTypedFindView {
    val cv: CardView = findView(TR.cv)
    val gameTitle = findView(TR.game_title)
    val totalRounds = findView(TR.total_rounds)
    val finishedImage = findView(TR.finished_image)
    val starBtn = findView(TR.star_button)
    val starsCount = findView(TR.star_counter)
    val seconds = findView(TR.total_seconds)
  }

}