package technology.walrath.m2h.data

import java.sql.DriverManager

import technology.walrath.m2h.h2.models.ProfileTO
import anorm._
import anorm.SqlParser._

/**
 * Created by justin on 2/22/15.
 */
class H2Push {
  def createPaste(owner: Long, pasteId: String, title: String, message: String, isPrivate: Boolean)(implicit c: java.sql.Connection): Unit = {
    SQL("insert into pastes(id, pasteId, ownerId, title, content, isPrivate) values(default, {pasteId}, {ownerId}, {title}, {content}, {isPrivate})")
      .on(
        'pasteId -> pasteId,
        'ownerId -> owner,
        'title -> title,
        'content -> message,
        'isPrivate -> isPrivate
      ).execute()
  }

  def createUserProfile(username: String, password: String, email: String)(implicit c: java.sql.Connection): Option[Long] = {
    SQL("insert into profiles(id, username, password, email) values(default, {username}, {password}, {email})").on(
      'username -> username,
      'password -> password,
      'email -> email
    ).executeInsert()
  }
}

object H2Push {
  implicit val con: java.sql.Connection = DriverManager.getConnection("jdbc:h2:/home/justin/Development/scala/lecarton/.data/lecarton", "sa", "")
}
