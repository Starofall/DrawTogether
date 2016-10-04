package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.H2Driver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(DrawingCommands.schema, DrawingContributes.schema, Drawings.schema, DrawingStars.schema, PlayEvolutions.schema, Users.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table DrawingCommands
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey
   *  @param userId Database column USER_ID SqlType(VARCHAR)
   *  @param drawingId Database column DRAWING_ID SqlType(VARCHAR)
   *  @param fromx Database column FROMX SqlType(INTEGER)
   *  @param fromy Database column FROMY SqlType(INTEGER)
   *  @param tox Database column TOX SqlType(INTEGER)
   *  @param toy Database column TOY SqlType(INTEGER)
   *  @param size Database column SIZE SqlType(INTEGER)
   *  @param color Database column COLOR SqlType(INTEGER) */
  case class DrawingCommandsRow(id: String, userId: String, drawingId: String, fromx: Int, fromy: Int, tox: Int, toy: Int, size: Int, color: Int)
  /** GetResult implicit for fetching DrawingCommandsRow objects using plain SQL queries */
  implicit def GetResultDrawingCommandsRow(implicit e0: GR[String], e1: GR[Int]): GR[DrawingCommandsRow] = GR{
    prs => import prs._
    DrawingCommandsRow.tupled((<<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table DRAWING_COMMANDS. Objects of this class serve as prototypes for rows in queries. */
  class DrawingCommands(_tableTag: Tag) extends Table[DrawingCommandsRow](_tableTag, "DRAWING_COMMANDS") {
    def * = (id, userId, drawingId, fromx, fromy, tox, toy, size, color) <> (DrawingCommandsRow.tupled, DrawingCommandsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(drawingId), Rep.Some(fromx), Rep.Some(fromy), Rep.Some(tox), Rep.Some(toy), Rep.Some(size), Rep.Some(color)).shaped.<>({r=>import r._; _1.map(_=> DrawingCommandsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey)
    /** Database column USER_ID SqlType(VARCHAR) */
    val userId: Rep[String] = column[String]("USER_ID")
    /** Database column DRAWING_ID SqlType(VARCHAR) */
    val drawingId: Rep[String] = column[String]("DRAWING_ID")
    /** Database column FROMX SqlType(INTEGER) */
    val fromx: Rep[Int] = column[Int]("FROMX")
    /** Database column FROMY SqlType(INTEGER) */
    val fromy: Rep[Int] = column[Int]("FROMY")
    /** Database column TOX SqlType(INTEGER) */
    val tox: Rep[Int] = column[Int]("TOX")
    /** Database column TOY SqlType(INTEGER) */
    val toy: Rep[Int] = column[Int]("TOY")
    /** Database column SIZE SqlType(INTEGER) */
    val size: Rep[Int] = column[Int]("SIZE")
    /** Database column COLOR SqlType(INTEGER) */
    val color: Rep[Int] = column[Int]("COLOR")
  }
  /** Collection-like TableQuery object for table DrawingCommands */
  lazy val DrawingCommands = new TableQuery(tag => new DrawingCommands(tag))

  /** Entity class storing rows of table DrawingContributes
   *  @param drawingId Database column DRAWING_ID SqlType(VARCHAR)
   *  @param userId Database column USER_ID SqlType(VARCHAR) */
  case class DrawingContributesRow(drawingId: String, userId: String)
  /** GetResult implicit for fetching DrawingContributesRow objects using plain SQL queries */
  implicit def GetResultDrawingContributesRow(implicit e0: GR[String]): GR[DrawingContributesRow] = GR{
    prs => import prs._
    DrawingContributesRow.tupled((<<[String], <<[String]))
  }
  /** Table description of table DRAWING_CONTRIBUTES. Objects of this class serve as prototypes for rows in queries. */
  class DrawingContributes(_tableTag: Tag) extends Table[DrawingContributesRow](_tableTag, "DRAWING_CONTRIBUTES") {
    def * = (drawingId, userId) <> (DrawingContributesRow.tupled, DrawingContributesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(drawingId), Rep.Some(userId)).shaped.<>({r=>import r._; _1.map(_=> DrawingContributesRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column DRAWING_ID SqlType(VARCHAR) */
    val drawingId: Rep[String] = column[String]("DRAWING_ID")
    /** Database column USER_ID SqlType(VARCHAR) */
    val userId: Rep[String] = column[String]("USER_ID")

    /** Primary key of DrawingContributes (database name CONSTRAINT_3) */
    val pk = primaryKey("CONSTRAINT_3", (drawingId, userId))
  }
  /** Collection-like TableQuery object for table DrawingContributes */
  lazy val DrawingContributes = new TableQuery(tag => new DrawingContributes(tag))

  /** Entity class storing rows of table Drawings
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey
   *  @param title Database column TITLE SqlType(VARCHAR)
   *  @param totalRounds Database column TOTAL_ROUNDS SqlType(INTEGER)
   *  @param secondsPerRound Database column SECONDS_PER_ROUND SqlType(INTEGER)
   *  @param base64PngImage Database column BASE64_PNG_IMAGE SqlType(VARCHAR)
   *  @param creationTimestamp Database column CREATION_TIMESTAMP SqlType(BIGINT) */
  case class DrawingsRow(id: String, title: String, totalRounds: Int, secondsPerRound: Int, base64PngImage: String, creationTimestamp: Long)
  /** GetResult implicit for fetching DrawingsRow objects using plain SQL queries */
  implicit def GetResultDrawingsRow(implicit e0: GR[String], e1: GR[Int], e2: GR[Long]): GR[DrawingsRow] = GR{
    prs => import prs._
    DrawingsRow.tupled((<<[String], <<[String], <<[Int], <<[Int], <<[String], <<[Long]))
  }
  /** Table description of table DRAWINGS. Objects of this class serve as prototypes for rows in queries. */
  class Drawings(_tableTag: Tag) extends Table[DrawingsRow](_tableTag, "DRAWINGS") {
    def * = (id, title, totalRounds, secondsPerRound, base64PngImage, creationTimestamp) <> (DrawingsRow.tupled, DrawingsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(title), Rep.Some(totalRounds), Rep.Some(secondsPerRound), Rep.Some(base64PngImage), Rep.Some(creationTimestamp)).shaped.<>({r=>import r._; _1.map(_=> DrawingsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey)
    /** Database column TITLE SqlType(VARCHAR) */
    val title: Rep[String] = column[String]("TITLE")
    /** Database column TOTAL_ROUNDS SqlType(INTEGER) */
    val totalRounds: Rep[Int] = column[Int]("TOTAL_ROUNDS")
    /** Database column SECONDS_PER_ROUND SqlType(INTEGER) */
    val secondsPerRound: Rep[Int] = column[Int]("SECONDS_PER_ROUND")
    /** Database column BASE64_PNG_IMAGE SqlType(VARCHAR) */
    val base64PngImage: Rep[String] = column[String]("BASE64_PNG_IMAGE")
    /** Database column CREATION_TIMESTAMP SqlType(BIGINT) */
    val creationTimestamp: Rep[Long] = column[Long]("CREATION_TIMESTAMP")
  }
  /** Collection-like TableQuery object for table Drawings */
  lazy val Drawings = new TableQuery(tag => new Drawings(tag))

  /** Entity class storing rows of table DrawingStars
   *  @param drawingId Database column DRAWING_ID SqlType(VARCHAR)
   *  @param userId Database column USER_ID SqlType(VARCHAR) */
  case class DrawingStarsRow(drawingId: String, userId: String)
  /** GetResult implicit for fetching DrawingStarsRow objects using plain SQL queries */
  implicit def GetResultDrawingStarsRow(implicit e0: GR[String]): GR[DrawingStarsRow] = GR{
    prs => import prs._
    DrawingStarsRow.tupled((<<[String], <<[String]))
  }
  /** Table description of table DRAWING_STARS. Objects of this class serve as prototypes for rows in queries. */
  class DrawingStars(_tableTag: Tag) extends Table[DrawingStarsRow](_tableTag, "DRAWING_STARS") {
    def * = (drawingId, userId) <> (DrawingStarsRow.tupled, DrawingStarsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(drawingId), Rep.Some(userId)).shaped.<>({r=>import r._; _1.map(_=> DrawingStarsRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column DRAWING_ID SqlType(VARCHAR) */
    val drawingId: Rep[String] = column[String]("DRAWING_ID")
    /** Database column USER_ID SqlType(VARCHAR) */
    val userId: Rep[String] = column[String]("USER_ID")

    /** Primary key of DrawingStars (database name CONSTRAINT_E) */
    val pk = primaryKey("CONSTRAINT_E", (drawingId, userId))
  }
  /** Collection-like TableQuery object for table DrawingStars */
  lazy val DrawingStars = new TableQuery(tag => new DrawingStars(tag))

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column ID SqlType(INTEGER), PrimaryKey
   *  @param hash Database column HASH SqlType(VARCHAR), Length(255,true)
   *  @param appliedAt Database column APPLIED_AT SqlType(TIMESTAMP)
   *  @param applyScript Database column APPLY_SCRIPT SqlType(CLOB)
   *  @param revertScript Database column REVERT_SCRIPT SqlType(CLOB)
   *  @param state Database column STATE SqlType(VARCHAR), Length(255,true)
   *  @param lastProblem Database column LAST_PROBLEM SqlType(CLOB) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.sql.Timestamp, applyScript: Option[java.sql.Clob], revertScript: Option[java.sql.Clob], state: Option[String], lastProblem: Option[java.sql.Clob])
  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[java.sql.Clob]], e4: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<?[java.sql.Clob], <<?[java.sql.Clob], <<?[String], <<?[java.sql.Clob]))
  }
  /** Table description of table PLAY_EVOLUTIONS. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "PLAY_EVOLUTIONS") {
    def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(INTEGER), PrimaryKey */
    val id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
    /** Database column HASH SqlType(VARCHAR), Length(255,true) */
    val hash: Rep[String] = column[String]("HASH", O.Length(255,varying=true))
    /** Database column APPLIED_AT SqlType(TIMESTAMP) */
    val appliedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("APPLIED_AT")
    /** Database column APPLY_SCRIPT SqlType(CLOB) */
    val applyScript: Rep[Option[java.sql.Clob]] = column[Option[java.sql.Clob]]("APPLY_SCRIPT")
    /** Database column REVERT_SCRIPT SqlType(CLOB) */
    val revertScript: Rep[Option[java.sql.Clob]] = column[Option[java.sql.Clob]]("REVERT_SCRIPT")
    /** Database column STATE SqlType(VARCHAR), Length(255,true) */
    val state: Rep[Option[String]] = column[Option[String]]("STATE", O.Length(255,varying=true))
    /** Database column LAST_PROBLEM SqlType(CLOB) */
    val lastProblem: Rep[Option[java.sql.Clob]] = column[Option[java.sql.Clob]]("LAST_PROBLEM")
  }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))

  /** Entity class storing rows of table Users
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey
   *  @param username Database column USERNAME SqlType(VARCHAR)
   *  @param saltedPassword Database column SALTED_PASSWORD SqlType(VARCHAR) */
  case class UsersRow(id: String, username: String, saltedPassword: String)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[String]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[String], <<[String], <<[String]))
  }
  /** Table description of table USERS. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "USERS") {
    def * = (id, username, saltedPassword) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(username), Rep.Some(saltedPassword)).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey)
    /** Database column USERNAME SqlType(VARCHAR) */
    val username: Rep[String] = column[String]("USERNAME")
    /** Database column SALTED_PASSWORD SqlType(VARCHAR) */
    val saltedPassword: Rep[String] = column[String]("SALTED_PASSWORD")

    /** Uniqueness Index over (username) (database name CONSTRAINT_INDEX_4) */
    val index1 = index("CONSTRAINT_INDEX_4", username, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
