package drawtogether.shared.ingame

/** Contains the classes for InGame */
object InGame {

  /** a user as part of the game */
  case class User(name: String)

  /** all relevant settings of a single game is stored here */
  case class GameSettings(title: String, totalRounds: Int, secondsPerRound: Int)


  /** a preview of a game used for the list of open games */
  case class InGamePreview(gameId: String, gameSettings: GameSettings, playerCount: Int, currentRound: Int, base64Image: String) {
    override def toString: String = "IngamePreview(~)"
  }
  /** finished user drawings */
  case class FinishedDrawing(gameId: String, gameSettings: GameSettings, var stars: Int, base64Image: String, var userStared: Boolean = false) {
    override def toString: String = "FinishedDrawing(~)"
  }


  /** a FSM of all state the game can be in */
  sealed trait GameState
  /** the game is waiting, it either has not started or to less players */
  case object WaitingState extends GameState
  /** all rounds are played and the game is done */
  case object DoneState extends GameState
  /** the game is running and the user has selected color and now has time to draw */
  case class UserDrawingState(user: User, remainingSeconds: Int) extends GameState
}
