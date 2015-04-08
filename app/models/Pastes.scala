package models

import scala.util.Random
import dao.PasteDao
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

class Pastes(tag: Tag) extends Table[Paste](tag, "pastes") {
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def pasteId = column[String]("pasteId", O.NotNull)
  def ownerId = column[Long]("ownerId", O.NotNull)
  def title = column[String]("title", O.NotNull)
  def content = column[String]("content", O.NotNull)
  def isPrivate = column[Boolean]("isPrivate", O.NotNull)
  def * = (id, pasteId, ownerId, title, content, isPrivate) <> (Paste.tupled, Paste.unapply)
}

object Pastes extends BaseSlickTrait[Paste] {
  def model = TableQueries.pastes

  def insert(p: Paste) = Try {
    DB withSession { implicit session =>
      val pasteWithId = p.copy(pasteId = PasteDao.generateRandomString(8))
      model += pasteWithId
      pasteWithId
    }
  }

  def byOwner(id: Long) = Try {
    DB withSession { implicit session =>
      model.filter(_.ownerId === id).list
    }
  }

  def byPasteId(pasteId: String) = Try {
    DB withSession { implicit session =>
      model.filter(_.pasteId === pasteId).list.headOption
    }
  }

  def updateVisibility(p: Paste) = Try {
    DB withSession { implicit session =>
      model.filter(_.id === p.id).map(_.isPrivate).update(p.isPrivate)
    }
  }

  def byTitle(title: String) = Try {
    DB withSession { implicit session =>
      model.filter(_.title like "%" + title + "%").list.headOption
    }
  }

  /**
   * Generates a sting of n length.
   */
  def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}