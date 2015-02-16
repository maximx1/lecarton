package dao

import scala.util.Random
import models.PasteTO
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

class PasteDao {
  val pasteToMapper = {
    get[Long]("id") ~
    get[String]("pasteId") ~
    get[Long]("ownerId") ~
    get[String]("title") ~
    get[String]("content") ~
    get[Boolean]("isPrivate") map {
      case id~pasteId~ownerId~title~content~isPrivate => PasteTO(id, pasteId, ownerId, title, content, isPrivate)
    }
  }

  /**
   * Creates a brand new paste.
   */
  def createPaste(owner: Long, title: String, message: String, isPrivate: Boolean): PasteTO = DB.withConnection(implicit c => {
    val newPasteId = PasteDao.generateRandomString(8)
    val insertedId = SQL("insert into pastes(id, pasteId, ownerId, title, content, isPrivate) values(default, {pasteId}, {ownerId}, {title}, {content}, {isPrivate}")
      .on(
        'pasteId -> newPasteId,
        'ownerId -> owner,
        'title -> title,
        'content -> message,
        'isPrivate -> isPrivate
      )
      .executeInsert()
    return PasteTO(insertedId, newPasteId, owner, title, message, isPrivate)
  })
    
  /**
   * Retrieves a paste based on the owner's id and if it's private or not.
   */
  def queryPastesOfOwner(pasteTO: PasteTO): List[PasteTO] = DB.withConnection(c => {
    SQL("select * from pastes where ownerId = {ownerId} and isPrivate = {isPrivate}")
      .on(
        'ownerId -> pasteTO.owner,
        'isPrivate -> pasteTO.isPrivate
      )
      .as(pasteToMapper *)
  })

  /**
   * Gets one paste from the database.
   */
  def queryPasteByPasteId(pasteTO: PasteTO): Option[PasteTO] = DB.withConnection(c => {
    val pastes = SQL("select * from pastes where pasteId = {pasteId}")
      .on(
        'pasteId -> pasteTO.pasteId
      )
      .as(pasteToMapper *)
    return if (pastes.isEmpty) None else Some(pastes(0))
  })

  /**
   * Updates a paste.
   * @param pasteTO The basis of the update.
   */
  def updatePaste(pasteTO: PasteTO) = DB.withConnection(c => {
    SQL("update pastes set isPrivate = {isPrivate} where id = {id}").on('isPrivate -> pasteTO.isPrivate, 'id -> pasteTO._id).execute()
  })

  /**
   * Gets all pastes that match the title string.
   * @param pasteTO The source query data.
   * @return All pastes matching the query.
   */
  def queryPasteByTitle(pasteTO: PasteTO): List[PasteTO] = DB.withConnection(c => {
    SQL("select * from pastes where title like %{searchString}%").on('searchString -> pasteTO.title).as(pasteToMapper *)
  })
}

object PasteDao {
  /**
   * Generates a sting of n length.
   */
  def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}