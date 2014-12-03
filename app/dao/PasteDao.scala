package dao

import com.mongodb.casbah.Imports._
import scala.util.Random
import models.PasteTO
import converters.PasteMongoConverters
import play.api.Play

class PasteDao {
  
	var mongodbName: String = "lecarton"
	var pasteCollectionName: String = "pastes"

  def getConnection = {
    val mongoUri = MongoClientURI(Play.current.configuration.getString("mongo.url").get)
    MongoClient(mongoUri)
  }

	/**
	 * Creates a brand new paste.
	 */
    def createPaste(owner: ObjectId, title: String, message: String, isPrivate: Boolean): MongoDBObject = {
        val mongoConnection = getConnection
        val collection = mongoConnection(mongodbName)(pasteCollectionName)
        val newObject = MongoDBObject(
            "pasteId" -> PasteDao.generateRandomString(8),
            "owner" -> owner,
            "title" -> title,
            "content" -> message,
            "isPrivate" -> isPrivate
        )
        collection += newObject
        return newObject
    }
    
	/**
	 * Retrieves a paste based on the owner's id and if it's private or not.
	 */
    def queryPastesOfOwner(pasteTO: PasteTO): List[PasteTO] = {
    	val query = MongoDBObject("owner" -> pasteTO.owner, "isPrivate" -> pasteTO.isPrivate)
    	queryMultiplePastesBase(query)
    }
    
    /**
     * Gets one paste from the database.
     */
    def queryPasteByPasteId(pasteTO: PasteTO): PasteTO = {
    	val query = MongoDBObject("pasteId" -> pasteTO.pasteId)
      querySinglePasteBase(query)
    }

  /**
   * Updates a paste.
   * @param pasteTO The basis of the update.
   */
  def updatePaste(pasteTO: PasteTO) = {
    val query = MongoDBObject("_id" -> pasteTO._id)
    val update = $set ("isPrivate" -> pasteTO.isPrivate)
    val dbConnection = Play.current.configuration.getString("mongo.url")
    val mongoConnection = MongoConnection(dbConnection.toString)
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
      val mongoConnection = getConnection
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
      val mongoConnection = getConnection
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