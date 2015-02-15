package dao

import com.mongodb.casbah.Imports._
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
  def createPaste(owner: Long, title: String, message: String, isPrivate: Boolean): PasteTo = DB.withConnection(implicit c => {
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
  def updatePaste(pasteTO: PasteTO) = {
    val query = MongoDBObject("_id" -> pasteTO._id)
    val update = $set ("isPrivate" -> pasteTO.isPrivate)
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(mongodbName)(pasteCollectionName)
    collection.update(query, update)
  }

  /**
   * Gets all pastes that match the title string.
   * @param pasteTO The source query data.
   * @return All pastes matching the query.
   */
    def queryPasteByTitle(pasteTO:PasteTO): List[PasteTO] = {
      val searchString = ".*" + pasteTO.title + ".*"
      val query = MongoDBObject("title" -> searchString.r, "isPrivate" -> pasteTO.isPrivate)
      queryMultiplePastesBase(query)
    }

  /**
   *  The base query for a single paste.
   * @param query Query for a single object.
   * @return The paste found or null.
   */
    def querySinglePasteBase(query: MongoDBObject): PasteTO = {
      val mongoConnection = MongoConnection()
      val collection = mongoConnection(mongodbName)(pasteCollectionName)
      PasteMongoConverters.convertFromMongoObject(
        collection.findOne(query) match {
          case Some(value) => value
          case None => null
        }
      )
    }

  /**
   * The base query for multiple pastes
   * @param query Query for multiple objects
   * @return The Pastes found or an empty list.
   */
    def queryMultiplePastesBase(query: MongoDBObject): List[PasteTO] = {
      val mongoConnection = MongoConnection()
      val collection = mongoConnection(mongodbName)(pasteCollectionName)
      collection.find(query).map(x => PasteMongoConverters.convertFromMongoObject(x)).toList
    }
}

object PasteDao {
  /**
   * Generates a sting of n length.
   */
  def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}