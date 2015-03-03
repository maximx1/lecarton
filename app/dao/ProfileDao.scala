package dao

import models.ProfileTO
import org.mindrot.jbcrypt.BCrypt
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

class ProfileDao {
  
  var anonUserId = 1

  val profileTOMapper = {
    get[Long]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("email") ~
    get[Boolean]("isAdmin") map {
      case id~username~password~email~isAdmin => ProfileTO(id, username, password, email, isAdmin)
    }
  }

  /**
   * Creates a brand new profile.
   */
  def createUserProfile(username: String, password: String, email: String): ProfileTO = DB.withConnection(implicit c => {
      val insertedId: Option[Long] = SQL("insert into profiles(id, username, password, email, isAdmin) values(default, {username}, {password}, {email}, default)").on(
        'username -> username,
        'password -> BCrypt.hashpw(password, BCrypt.gensalt(4)),
        'email -> email
      ).executeInsert()
      return ProfileTO(insertedId.get, username, null, email, false)
  })

	/**
	 * Retrieves a profile using the username.
	 */
    def queryUserProfileByUsername(profileTO: ProfileTO): Option[ProfileTO] = DB.withConnection(implicit c => {
      val results = SQL("select id, username, password, email, isAdmin from profiles where username = {username}").on(
        'username -> profileTO.username
      ).as(profileTOMapper *)
      return if (results.isEmpty) None else Some(results(0))
    })

  /**
   * Retrieves a profile using the id.
   */
  def queryUserProfileById(profileTO: ProfileTO): Option[ProfileTO] = DB.withConnection(implicit c => {
    val results = SQL("select id, username, password, email, isAdmin from profiles where id = {id}").on(
      'id -> profileTO._id
    ).as(profileTOMapper *)
    return if (results.isEmpty) None else Some(results(0))
  })

  /**
   * Gets a count of all the profiles in the db.
   * @return The count.
   */
  def profileCount: Long = DB.withConnection(implicit c => SQL("select count(*) as c from profiles").apply().head[Long]("c"))
}