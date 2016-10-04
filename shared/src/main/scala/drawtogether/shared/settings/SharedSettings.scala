package drawtogether.shared.settings

/** Common settings on client and server */
object SharedSettings {

  /** pixel size used for image */
  final val IMAGE_SIZE = 512

  /** the url of our server */
  //final var SERVER_URL = "ws://localhost:9000/socket" // DEVELOPMENT MODE

  final var SERVER_URL = "ws://altnorm.de:10010/socket" // PRODUCTION MODE

}
