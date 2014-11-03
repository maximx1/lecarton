package dao

import com.mongodb.casbah.Imports._
import scala.util.Random
import models.PasteTO
import converters.PasteMongoConverters

object PasteDao {
  
	var mongodbName: String = "lecarton"
	var pasteCollectionName: String = "pastes"
	
	/**
	 * Creates a brand new paste.
	 */
    def createPaste(owner: ObjectId, title: String, message: String, isPrivate: Boolean): MongoDBObject = {
        val mongoConnection = MongoConnection()
        val collection = mongoConnection(mongodbName)(pasteCollectionName)
        val newObject = MongoDBObject(
            "pasteId" -> generateRandomString(8),
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
    
    /**
     * A blanket exception to be thrown.
     */
    def mongoFail = throw new MongoException("Document not found")
    
    /**
     * Generates a sting of n length.
     */
    def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}