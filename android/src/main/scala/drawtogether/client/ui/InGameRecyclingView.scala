package drawtogether.client.ui

import android.graphics.BitmapFactory
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.util.Base64
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import drawtogether.client.services.DrawTogetherActivity
import drawtogether.client.util.ViewTypedFindView
import drawtogether.client.{R, TR}
import drawtogether.shared.communication.SharedMessages.CJoinGameRequest
import drawtogether.shared.ingame.InGame.InGamePreview

/** This is a trait to represent and update of all running games on UI */
trait InGameRecyclingView {
  /*the trait is able to use the fields or methods of a class DrawTogetherActivity (mix in),
   this is done by specifying a self type for the trait.*/
  self: DrawTogetherActivity =>

  /** local copy of the list of running games */
  private var runningGames = List[InGamePreview]()
  /** recycler view in element_list_view_content.xml */
  lazy val recList = findView(TR.channelListRect)
  /** adapter for all running games */
  private val adapter = new RVAdapterInGame()
  /** default adapter if the are not running games */
  private val defaultAdapter = new RVAdapterDefault()

  /**
    * Store the list of the games from server into the local copy
    * Depend on  size of the list choose the adapter
    *
    * @param inGamePreviews : list of InGamePreviews with gameId, gameSettings, playerCount, currentRound, base64Image
    */
  def setRunningGames(inGamePreviews: List[InGamePreview]): Unit = {
    runningGames = inGamePreviews
    runningGames.isEmpty match {
      //if there are no running games in the list
      case true => recList.setAdapter(defaultAdapter)
      //otherwise
      case false => recList.setAdapter(adapter)
    }
    //notify adapter about changes on the list of running games
    adapter.notifyDataSetChanged()
  }

  onCreate {
    recList.setHasFixedSize(true)
    val llm = new LinearLayoutManager(this)
    recList.setLayoutManager(llm)
    recList.setAdapter(defaultAdapter)
  }

  /**
    * Adapter manages the card view representation for all running games
    * If the data will get updated the adapter changes the values on gui as well
    */
  class RVAdapterInGame extends RecyclerView.Adapter[InGameViewHolder] {

    /** returns the number of runningGames */
    override def getItemCount: Int = runningGames.size

    /** fills the gui elements with data */
    override def onBindViewHolder(vh: InGameViewHolder, i: Int): Unit = {
      val currentGame = runningGames(i)
      val settings = currentGame.gameSettings
      // set values
      vh.ingameTitle.setText(settings.title)
      vh.ingameCurrentRound.setText(s"${currentGame.currentRound} / ${settings.totalRounds}")
      vh.peopleCount.setText(s"${currentGame.playerCount} / 4")
      vh.seconds.setText(s"${settings.secondsPerRound} / ${settings.totalRounds * settings.secondsPerRound}")
      // set image
      val buffer = Base64.decode(currentGame.base64Image, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length)
      vh.ingamePicture.setImageBitmap(bitmap)
      vh.cv.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = sendMessage(CJoinGameRequest(runningGames(i).gameId))
      })
    }

    /** returns a view for runningGame */
    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): InGameViewHolder = {
      new InGameViewHolder(
        LayoutInflater.from(viewGroup.getContext).inflate(R.layout.card_layout_ingame, viewGroup, false)
      )
    }
  }

  /** Layout group to be inflated and updated */
  class InGameViewHolder(val view: View) extends RecyclerView.ViewHolder(view) with ViewTypedFindView {
    val cv = findView(TR.ingame_cv)
    val ingameTitle = findView(TR.ingame_title)
    val ingameCurrentRound = findView(TR.ingame_current_round)
    val ingamePicture = findView(TR.ingame_picture)
    val peopleCount = findView(TR.ingame_people_count)
    val seconds = findView(TR.ingame_seconds)
  }

  /** Default adapter if there are no running games */
  class RVAdapterDefault extends RecyclerView.Adapter[InGameViewHolder] {

    /** returns one, as we just want to show one empty notification */
    override def getItemCount: Int = 1

    /** fills the gui elements with default data */
    override def onBindViewHolder(vh: InGameViewHolder, i: Int): Unit = {}

    /** creates a view for a empty list */
    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): InGameViewHolder = {
      new InGameViewHolder(
        LayoutInflater.from(viewGroup.getContext).inflate(R.layout.card_layout_empty, viewGroup, false)
      )
    }
  }

}
