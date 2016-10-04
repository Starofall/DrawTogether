package drawtogether.client.services

import drawtogether.client.drawing.AndroidDrawTarget
import drawtogether.client.services.LocalDataService.RunningGame
import drawtogether.shared.ingame.InGame.GameSettings
import org.scaloid.common._

/** Data Service to keep the data persistent across activity changes */
class LocalDataService extends LocalService {

  /** information about the current game */
  var currentGame: Option[RunningGame] = None

  /* the drawTarget holding the image as it is edited */
  val drawTarget: AndroidDrawTarget = AndroidDrawTarget()
}

object LocalDataService {
  /** an helper class for a running game that is joined, can have an image attached to if the user is joining */
  case class RunningGame(gameId: String, settings: GameSettings, base64OriginalImage: Option[String])
}

