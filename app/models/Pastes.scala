package models

import utils.RandomUtils

import scala.util.Random
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._

import scala.util.Try

case class Paste(
  id: Option[Long],
  pasteId: String,
  ownerId: Long,
  title: String,
  content: String,
  isPrivate: Boolean
) extends Model

class PastesTable(tag: Tag) extends Table[Paste](tag, "PASTES") {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def pasteId = column[String]("PASTEID", O.NotNull)
  def ownerId = column[Long]("OWNERID", O.NotNull)
  def title = column[String]("TITLE", O.NotNull)
  def content = column[String]("CONTENT", O.NotNull)
  def isPrivate = column[Boolean]("ISPRIVATE", O.NotNull)
  def * = (id, pasteId, ownerId, title, content, isPrivate) <> (Paste.tupled, Paste.unapply)
}

class Pastes extends BaseSlickTrait[Paste] {
  def model = TableQueries.pastes

  def insert(p: Paste): Try[Paste] = Try {
    DB withSession { implicit session =>
      val pasteWithId = p.copy(pasteId = RandomUtils.generateRandomString(8))
      model += pasteWithId
      pasteWithId
    }
  }

  def byOwner(id: Long): Try[List[Paste]] = Try {
    DB withSession { implicit session =>
      model.filter(_.ownerId === id).list
    }
  }

  def byPasteId(pasteId: String): Try[Option[Paste]] = Try {
    DB withSession { implicit session =>
      model.filter(_.pasteId === pasteId).list.headOption
    }
  }

  def updateVisibility(p: Paste): Try[Int] = Try {
    DB withSession { implicit session =>
      model.filter(_.id === p.id).map(_.isPrivate).update(p.isPrivate)
    }
  }

  def byTitle(title: String): Try[List[Paste]] = Try {
    DB withSession { implicit session =>
      model.filter(_.title like "%" + title + "%").list
    }
  }

  /**
   * Generates a sting of n length.
   */
  def generateRandomString(length: Int): String = Random.alphanumeric.take(length).mkString
}