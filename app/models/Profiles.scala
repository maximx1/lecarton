package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import org.mindrot.jbcrypt.BCrypt
import scala.util.Try

case class Profile(
  id: Option[Long],
  username: String,
  password: String,
  email: String,
  isAdmin: Boolean
) extends Model

class ProfilesTable(tag: Tag) extends Table[Profile](tag, "PROFILES") {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def username = column[String]("USERNAME", O.NotNull)
  def password = column[String]("PASSWORD", O.NotNull)
  def email = column[String]("EMAIL", O.NotNull)
  def isAdmin = column[Boolean]("ISADMIN", O.NotNull)
  def * = (id, username, password, email, isAdmin) <> (Profile.tupled, Profile.unapply)
}

class Profiles extends BaseSlickTrait[Profile] {
  def model = TableQueries.profiles

  override def +=(p: Profile): Try[Int] = super.+=(p.copy(password = BCrypt.hashpw(p.password, BCrypt.gensalt(4))))

  def byUsername(username: String): Try[Option[Profile]] = Try {
    DB withSession { implicit session =>
      model.filter(_.username === username).list.headOption
    }
  }

  def byId(id: Long): Try[Option[Profile]] = Try {
    DB withSession { implicit session =>
      model.filter(_.id === id).list.headOption
    }
  }
}