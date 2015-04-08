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

class Profiles(tag: Tag) extends Table[Profile](tag, "profiles") {
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username", O.NotNull)
  def password = column[String]("password", O.NotNull)
  def email = column[String]("email", O.NotNull)
  def isAdmin = column[Boolean]("isAdmin", O.NotNull)
  def * = (id, username, password, email, isAdmin) <> (Profile.tupled, Profile.unapply)
}

object Profiles extends BaseSlickTrait[Profile] {
  def model = TableQueries.profiles

  override def +=(p: Profile) = super.+=(p.copy(password = BCrypt.hashpw(p.password, BCrypt.gensalt(4))))

  def byUsername(username: String) = Try {
    DB withSession { implicit session =>
      model.filter(_.username === username).list.headOption
    }
  }

  def byId(id: Long) = Try {
    DB withSession { implicit session =>
      model.filter(_.id === id).list.headOption
    }
  }
}