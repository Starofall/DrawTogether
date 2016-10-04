package drawtogether.shared.communication

import drawtogether.shared.drawing.DrawingObjects.DrawCommand
import drawtogether.shared.ingame.InGame._
import prickle.{CompositePickler, PicklerPair}


/** Contains all messages that can be send between server and client  */
object SharedMessages {

  trait PicklerTrait {
    implicit var messagePickler: PicklerPair[NetworkMessage] = SharedMessages.messagePickler
  }

  implicit val gameStatePickler = CompositePickler[GameState]
    .concreteType[WaitingState.type]
    .concreteType[DoneState.type]
    .concreteType[UserDrawingState]

  implicit var messagePickler: PicklerPair[NetworkMessage] = CompositePickler[NetworkMessage]

  //@formatter:off

  /** an actorMessage is anything that is send between actors */
  trait ActorMessage

  /** An NetworkMessage is something the server and the client exchange thought Akka/WebSocket */
  sealed trait NetworkMessage extends ActorMessage

  /** a message that only travels in the direction from the server to the client */
  trait ServerToClient extends NetworkMessage

  /** a message that only travels in the direction from the client to the server */
  trait ClientToServer extends NetworkMessage

  /** the messages that are part of the game protocol */
  trait GameMessage extends ActorMessage


  // Connection
  case class CKeepAlive()                                                             extends ClientToServer
  case class SKeepAlive()                                                             extends ServerToClient
  messagePickler = messagePickler.concreteType[CKeepAlive].concreteType[SKeepAlive]

  case class CUserDisconnected()                                                      extends ClientToServer
  case class SNotAuthorized(reason:String)                                            extends ServerToClient
  messagePickler = messagePickler.concreteType[CUserDisconnected].concreteType[SNotAuthorized]

  // Login
  case class CLoginRequest(username: String,password:String)                          extends ClientToServer
  case class SLoginSuccessFul()                                                       extends ServerToClient
  case class SLoginFailed(reason:String)                                              extends ServerToClient
  messagePickler = messagePickler.concreteType[CLoginRequest].concreteType[SLoginSuccessFul].concreteType[SLoginFailed]

  // Register
  case class CRegisterRequest(username: String,password:String)                       extends ClientToServer
  case class SRegisterSuccessFul(username:String)                                     extends ServerToClient
  case class SRegisterFailed(reason:String)                                           extends ServerToClient
  messagePickler = messagePickler.concreteType[CRegisterRequest].concreteType[SRegisterSuccessFul].concreteType[SRegisterFailed]

  // Join game
  case class CJoinGameRequest(gameId:String)                                          extends ClientToServer
  case class SJoinGameSuccessful(gameId:String,gameSettings: GameSettings,base64pngImage:Option[String])            extends ServerToClient
  case class SJoinGameRejected(reason:String)                                         extends ServerToClient
  messagePickler = messagePickler.concreteType[CJoinGameRequest].concreteType[SJoinGameSuccessful].concreteType[SJoinGameRejected]

  // requests for drawings
  case class CHighScoreDrawingsRequest()                                              extends ClientToServer
  case class SHighScoreDrawingsResponse(bestDrawings:List[FinishedDrawing])           extends ServerToClient
  messagePickler = messagePickler.concreteType[CHighScoreDrawingsRequest].concreteType[SHighScoreDrawingsResponse]

  case class CUserDrawingsRequest()                                                   extends ClientToServer
  case class SUserDrawingsResponse (usersDrawings:List[FinishedDrawing])              extends ServerToClient
  messagePickler = messagePickler.concreteType[CUserDrawingsRequest].concreteType[SUserDrawingsResponse]


  case class CRecentDrawingsRequest()                                                 extends ClientToServer
  case class SRecentDrawingsResponse(recentDrawings: List[FinishedDrawing])           extends ServerToClient
  messagePickler = messagePickler.concreteType[CRecentDrawingsRequest].concreteType[SRecentDrawingsResponse]

  //the original picture
  case class COriginalDrawingRequest(gameId:String)                                                 extends ClientToServer
  case class SOriginalDrawingResponse(gameId:String,base64Image:String)           extends ServerToClient
  messagePickler = messagePickler.concreteType[COriginalDrawingRequest].concreteType[SOriginalDrawingResponse]

  case class CRunningGameListRequest()                                                extends ClientToServer
  case class SRunningGameListResponse(previews: List[InGamePreview])                  extends ServerToClient
  messagePickler = messagePickler.concreteType[CRunningGameListRequest].concreteType[SRunningGameListResponse]

  // game messages

  /** user wants to create a new game */
  case class CCreateGameRequest(gameSettings: GameSettings)                           extends ClientToServer
  messagePickler = messagePickler.concreteType[CCreateGameRequest]

  /** user has drawn something */
  case class CGameDrawCommand(drawCommand: DrawCommand)                               extends ClientToServer with GameMessage
  /** another user has drawn something */
  case class SGameDrawCommand(drawCommand: DrawCommand)                               extends ServerToClient
  messagePickler = messagePickler.concreteType[CGameDrawCommand].concreteType[SGameDrawCommand]


  /** user wants to leave its current game */
  case class CGameLeaveRequest()                                                      extends ClientToServer with GameMessage
  messagePickler = messagePickler.concreteType[CGameLeaveRequest]

  /** server updates the user about the current/new state */
  case class SGameState(gameState: GameState,roundNumber:Int)                                         extends ServerToClient with GameMessage
  case class SGameUserList(users: List[User])                                              extends ServerToClient with GameMessage
  messagePickler = messagePickler.concreteType[SGameState].concreteType[SGameUserList]

  /** user stars the view in one game*/
  case class CStaredRequest(ifStared:Boolean,gameId: String)                          extends ClientToServer
  messagePickler = messagePickler.concreteType[CStaredRequest]


}
